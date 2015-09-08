/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.handler;

import io.beanmapper.BeanMapper;
import io.restify.CrudConfig;
import io.restify.EntityInformation;
import io.restify.security.SecurityProvider;
import io.restify.service.CrudService;
import io.restify.swagger.SwaggerApiDescriptor;
import io.restify.util.PageableResolver;
import io.restify.util.UrlUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Default implementation of the {@link EntityHandlerMappingFactory}.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class DefaultEntityHandlerMappingFactory implements EntityHandlerMappingFactory {
    
    private final ObjectMapper objectMapper;
    
    private final ConversionService conversionService;
    
    private final BeanMapper beanMapper;
    
    private final SecurityProvider securityProvider;
    
    /**
     * Instantiate a new {@link DefaultEntityHandlerMappingFactory}.
     * 
     * @param objectMapper
     *              the {@link ObjectMapper} for JSON parsing and formatting
     * @param conversionService
     *              the {@link ConversionService} for converting between types
     * @param beanMapper
     *              the {@link BeanMapper} for mapping between beans
     * @param securityProvider
     *              the {@link SecurityProvider} checking the authorization
     */
    public DefaultEntityHandlerMappingFactory(ObjectMapper objectMapper,
                                         ConversionService conversionService,
                                                BeanMapper beanMapper,
                                          SecurityProvider securityProvider) {
        this.objectMapper = objectMapper;
        this.conversionService = conversionService;
        this.beanMapper = beanMapper;
        this.securityProvider = securityProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultHandlerMapping build(CrudService<?, ?> service, EntityInformation information) {
        return new DefaultHandlerMapping(new DefaultCrudController(service, information));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private class DefaultCrudController {

        private final CrudService service;
        
        private final EntityInformation information;
        
        public DefaultCrudController(CrudService service, EntityInformation information) {
            this.service = service;
            this.information = information;
        }
        
        /**
         * Retrieve all entities.
         * 
         * @return the entities, in result type
         */
        @ResponseBody
        public Object findAll(HttpServletRequest request) {
            checkIsAuthorized(information.findAll().roles());

            if (PageableResolver.isSupported(request)) {
                return findAllAsPage(request);
            } else {
                return findAllAsCollection(request);
            }
        }

        private void checkIsAuthorized(String[] roles) {
            if (!securityProvider.isAuthorized(roles)) {
                throw new SecurityException("Not authorized, should be one of: " + StringUtils.join(roles, ", "));
            }
        }

        /**
         * Retrieve all entities in a page.
         * 
         * @param request the request
         * @return the page of entities, in result type
         */
        private Page findAllAsPage(HttpServletRequest request) {
            Pageable pageable = PageableResolver.getPageable(request, information.getEntityClass());
            Page<?> result = service.findAll(pageable);
            if (result.hasContent()) {
                List<?> content = new ArrayList(result.getContent());
                List<?> transformed = new ArrayList(beanMapper.map(content, information.getResultType(information.findAll())));
                result = new PageImpl(transformed, pageable, result.getTotalElements());
            }
            return result;
        }

        /**
         * Retrieve all entities in a collection.
         * 
         * @return the collection of entities, in result type
         */
        private Collection findAllAsCollection(HttpServletRequest request) {
            Sort sort = PageableResolver.getSort(request, information.getEntityClass());
            List<?> entities = service.findAll(sort);
            return beanMapper.map(entities, information.getResultType(information.findAll()));
        }

        /**
         * Retrieve a single entity by identifier: /{id}
         * 
         * @param id the identifier
         * @return the entity, in result type
         */
        @ResponseBody
        public Object findOne(HttpServletRequest request) {
            checkIsAuthorized(information.findOne().roles());

            Object entity = getEntityById(request);
            return beanMapper.map(entity, information.getResultType(information.findOne()));
        }
        
        private Serializable extractId(HttpServletRequest request) {
            String path = UrlUtils.getPath(request);
            String raw = StringUtils.substringAfterLast(path, UrlUtils.SLASH);
            return conversionService.convert(raw, information.getIdentifierClass().asSubclass(Serializable.class));
        }
        
        /**
         * Creates a new entity. Any content is retrieved from the request body.
         * 
         * @return the entity
         */
        @ResponseBody
        public Object create(HttpServletRequest request) throws Exception {
            checkIsAuthorized(information.create().roles());

            Object input = objectMapper.readValue(request.getReader(), information.getInputType(information.create()));
            Persistable<?> entity = beanMapper.map(input, information.getEntityClass());
            Persistable<?> output = service.save(entity);
            return convert(output, information.getResultType(information.create()));
        }
        
        /**
         * Updates an entity. Any content is retrieved from the request body.
         * 
         * @return the updated entity
         */
        @ResponseBody
        public Object update(HttpServletRequest request) throws Exception {
            checkIsAuthorized(information.update().roles());

            Object input = objectMapper.readValue(request.getReader(), information.getInputType(information.update()));
            Persistable<?> entity = getEntityById(request);
            Persistable<?> output = service.save(beanMapper.map(input, entity));
            return convert(output, information.getResultType(information.update()));
        }

        private Persistable<?> getEntityById(HttpServletRequest request) {
            Serializable id = extractId(request);
            Persistable<?> entity = service.findOne(id);
            if (entity == null) {
                throw new IllegalArgumentException("Could not find entity '" + information.getEntityClass().getSimpleName() + "' with id: " + id);
            }
            return entity;
        }
        
        /**
         * Deletes an entity based on an identifier: /{id}
         * 
         * @return the deleted entity
         */
        @ResponseBody
        public Object delete(HttpServletRequest request) {
            checkIsAuthorized(information.delete().roles());

            Persistable<?> entity = getEntityById(request);
            service.delete(entity);
            return convert(entity, information.getResultType(information.delete()));
        }
        
        /**
         * Enhances our bean mapper with some common non-bean types that can be returned. 
         * 
         * @param entity the entity
         * @param targetType the target type
         * @return the converted object
         */
        private Object convert(Persistable<?> entity, Class<?> targetType) {
            if (Void.class.equals(targetType)) {
                return null;
            } else if (information.getIdentifierClass().equals(targetType)) {
                return entity.getId();
            } else {
                return beanMapper.map(entity, targetType);
            }
        }

    }
    
    /**
     * Maps our requests to controller handle methods.
     *
     * @author Jeroen van Schagen
     * @since Sep 3, 2015
     */
    private static class DefaultHandlerMapping extends EntityHandlerMapping implements SwaggerApiDescriptor {
        
        private final DefaultCrudController controller;
        
        public DefaultHandlerMapping(DefaultCrudController controller) {
            super(controller.information);
            this.controller = controller;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Object getHandlerInternal(HttpServletRequest request) throws Exception {
            Object result = null;
            Method method = findMethod(request);
            if (method != null) {
                result = new HandlerMethod(controller, method);
            }
            return result;
        }
        
        /**
         * Dynamically match our servlet request to one of the service
         * methods. Whenever a request is recognized we return that method.
         * 
         * @param request the incomming request
         * @return the matching method, if any
         */
        private Method findMethod(HttpServletRequest request) throws NoSuchMethodException {
            Method method = null;
            int fragments = UrlUtils.getPath(request).split(UrlUtils.SLASH).length;
            if (fragments == 2) {
                if (RequestMethod.GET.name().equals(request.getMethod())) {
                    method = findMethodIfEnabled("findAll", controller.information.findAll());
                } else if (RequestMethod.POST.name().equals(request.getMethod())) {
                    method = findMethodIfEnabled("create", controller.information.create());
                }
            } else if (fragments == 3) {
                if (RequestMethod.GET.name().equals(request.getMethod())) {
                    method = findMethodIfEnabled("findOne", controller.information.findOne());
                } else if (RequestMethod.PUT.name().equals(request.getMethod())) {
                    method = findMethodIfEnabled("update", controller.information.update());
                } else if (RequestMethod.DELETE.name().equals(request.getMethod())) {
                    method = findMethodIfEnabled("delete", controller.information.delete());
                }
            }
            return method;
        }
        
        private Method findMethodIfEnabled(String methodName, CrudConfig config) throws NoSuchMethodException {
            Method method = null;
            if (config.enabled()) {
                method = controller.getClass().getMethod(methodName, HttpServletRequest.class);
            }
            return method;
        }

        /**
         * {@inheritDoc}
         * <br><br>
         * <b>All swagger dependencies are specified with their full name to prevent
         * class loading errors for users without swagger.</b>
         */
        @Override
        public void enhance(com.mangofactory.swagger.models.dto.ApiListing listing,
                            com.mangofactory.swagger.models.ModelProvider modelProvider) {
            new DefaultSwaggerDescriber(modelProvider, getInformation()).enhance(listing);
        }
        
    }
    
    /**
     * Describes our dynamically generated controller handle methods.
     *
     * @author Jeroen van Schagen
     * @since Sep 3, 2015
     */
    private static class DefaultSwaggerDescriber {
        
        private final com.mangofactory.swagger.models.ModelProvider modelProvider;
        
        private final EntityInformation information;
        
        private final String basePath;

        public DefaultSwaggerDescriber(com.mangofactory.swagger.models.ModelProvider modelProvider, EntityInformation information) {
            this.modelProvider = modelProvider;
            this.information = information;
            
            String basePath = information.getBasePath();
            if (!basePath.startsWith("/")) {
                basePath = "/" + basePath;
            }
            this.basePath = basePath;
        }
        
        /**
         * Enhances the swagger API listings with new models and descriptions.
         */
        void enhance(com.mangofactory.swagger.models.dto.ApiListing listing) {
            if (information.findAll().enabled()) {
                registerModel(listing, information.getResultType(information.findAll()));
                newDescription("findAll", basePath, RequestMethod.GET)
                    .responseClassIterable(information.getResultType(information.findAll()))
                    .addQueryParameter("page", Long.class, false)
                    .addQueryParameter("size", Long.class, false)
                    .addQueryParameter("sort", String.class, false)
                    .register(listing);
            }
            
            if (information.findOne().enabled()) {
                registerModel(listing, information.getResultType(information.findOne()));
                newDescription("findOne", basePath + "/{id}", RequestMethod.GET)
                    .responseClass(information.getResultType(information.findOne()))
                    .addPathParameter("id", information.getIdentifierClass())
                    .register(listing);
            }
            
            if (information.create().enabled()) {
                registerModel(listing, information.getInputType(information.create()));
                registerModel(listing, information.getResultType(information.create()));
                newDescription("create", basePath, RequestMethod.POST)
                    .responseClass(information.getResultType(information.create()))
                    .addBodyParameter(information.getInputType(information.create()))
                    .register(listing);
            }
            
            if (information.update().enabled()) {
                registerModel(listing, information.getInputType(information.update()));
                registerModel(listing, information.getResultType(information.update()));
                newDescription("update", basePath + "/{id}", RequestMethod.PUT)
                    .responseClass(information.getResultType(information.update()))
                    .addPathParameter("id", information.getIdentifierClass())
                    .addBodyParameter(information.getInputType(information.update()))
                    .register(listing);
            }
            
            if (information.delete().enabled()) {
                registerModel(listing, information.getResultType(information.delete()));
                newDescription("delete", basePath + "/{id}", RequestMethod.DELETE)
                    .responseClass(information.getResultType(information.delete()))
                    .addPathParameter("id", information.getIdentifierClass())
                    .register(listing);
            }
        }

        private void registerModel(com.mangofactory.swagger.models.dto.ApiListing listing, Class<?> modelType) {
            io.restify.swagger.SwaggerUtils.addIfNotExists(listing, modelType, modelProvider);
        }
        
        private io.restify.swagger.SwaggerUtils.DescriptionBuilder newDescription(String description, String path, RequestMethod method) {
            return io.restify.swagger.SwaggerUtils.newDescription(description, path, method);
        }

    }
    
}

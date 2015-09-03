/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.handler;

import io.beanmapper.BeanMapper;
import io.restify.EntityInformation;
import io.restify.service.CrudService;
import io.restify.swagger.SwaggerApiDescriptor;
import io.restify.util.PageableResolver;
import io.restify.util.UrlUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    
    /**
     * Instantiate a new {@link DefaultEntityHandlerMappingFactory}.
     * 
     * @param objectMapper
     *              the {@link ObjectMapper} for JSON parsing and formatting
     * @param conversionService
     *              the {@link ConversionService} for converting between types
     * @param beanMapper
     *              the {@link BeanMapper} for mapping between beans
     */
    public DefaultEntityHandlerMappingFactory(ObjectMapper objectMapper,
                                       ConversionService conversionService,
                                              BeanMapper beanMapper) {
        this.objectMapper = objectMapper;
        this.conversionService = conversionService;
        this.beanMapper = beanMapper;
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
            if (PageableResolver.isSupported(request)) {
                return findAllAsPage(request);
            } else {
                return findAllAsCollection(request);
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
                List<?> transformed = new ArrayList(beanMapper.map(content, information.getResultType()));
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
            return beanMapper.map(entities, information.getResultType());
        }

        /**
         * Retrieve a single entity by identifier: /{id}
         * 
         * @param id the identifier
         * @return the entity, in result type
         */
        @ResponseBody
        public Object findOne(HttpServletRequest request) {
            Object entity = getEntityById(request);
            return beanMapper.map(entity, information.getResultType());
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
            Object input = objectMapper.readValue(request.getReader(), information.getCreateType());
            Object entity = beanMapper.map(input, information.getEntityClass());
            Object output = service.save(entity);
            return beanMapper.map(output, information.getResultType());
        }
        
        /**
         * Updates an entity. Any content is retrieved from the request body.
         * 
         * @return the updated entity
         */
        @ResponseBody
        public Object update(HttpServletRequest request) throws Exception {
            Object entity = getEntityById(request);
            Object input = objectMapper.readValue(request.getReader(), information.getUpdateType());
            Object output = service.save(beanMapper.map(input, entity));
            return beanMapper.map(output, information.getResultType());
        }

        private Object getEntityById(HttpServletRequest request) {
            Serializable id = extractId(request);
            Object entity = service.findOne(id);
            if (entity == null) {
                throw new IllegalArgumentException("Could not find entity '" + information.getEntityClass().getSimpleName() + "' with id: " + id);
            }
            return entity;
        }
        
        /**
         * Deletes an entity based on an identifier: /{id}
         */
        @ResponseBody
        public void delete(HttpServletRequest request) {
            Serializable id = extractId(request);
            service.delete(id);
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
            if (getInformation().isReadonly() && !RequestMethod.GET.name().equals(request.getMethod())) {
                return null; // Skip anything but GET requests when read only
            }

            Method method = null;
            int fragments = UrlUtils.getPath(request).split(UrlUtils.SLASH).length;
            if (fragments == 2) {
                if (RequestMethod.GET.name().equals(request.getMethod())) {
                    method = DefaultCrudController.class.getMethod("findAll", HttpServletRequest.class);
                } else if (RequestMethod.POST.name().equals(request.getMethod())) {
                    method = DefaultCrudController.class.getMethod("create", HttpServletRequest.class);
                }
            } else if (fragments == 3) {
                if (RequestMethod.GET.name().equals(request.getMethod())) {
                    method = DefaultCrudController.class.getMethod("findOne", HttpServletRequest.class);
                } else if (RequestMethod.PUT.name().equals(request.getMethod())) {
                    method = DefaultCrudController.class.getMethod("update", HttpServletRequest.class);
                } else if (RequestMethod.DELETE.name().equals(request.getMethod())) {
                    method = DefaultCrudController.class.getMethod("delete", HttpServletRequest.class);
                }
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
        public void addApiDescriptions(com.mangofactory.swagger.models.dto.ApiListing apiListing) {
            new DefaultSwaggerDescriber(getInformation()).addAll(apiListing);
        }
        
    }
    
    /**
     * Describes our dynamically generated controller handle methods.
     *
     * @author Jeroen van Schagen
     * @since Sep 3, 2015
     */
    private static class DefaultSwaggerDescriber {
        
        private final EntityInformation information;

        public DefaultSwaggerDescriber(EntityInformation information) {
            this.information = information;
        }
        
        void addAll(com.mangofactory.swagger.models.dto.ApiListing apiListing) {
            addModels(apiListing);
            addApiDescriptions(apiListing);
        }
        
        // Models

        private void addModels(com.mangofactory.swagger.models.dto.ApiListing apiListing) {
            // TODO Auto-generated method stub
        }

        // API descriptions

        private void addApiDescriptions(com.mangofactory.swagger.models.dto.ApiListing apiListing) {
            addApiDescriptionIfNotExists(apiListing, getAll());
            addApiDescriptionIfNotExists(apiListing, getOne());
            addApiDescriptionIfNotExists(apiListing, create());
            addApiDescriptionIfNotExists(apiListing, update());
            addApiDescriptionIfNotExists(apiListing, delete());
        }
        
        void addApiDescriptionIfNotExists(com.mangofactory.swagger.models.dto.ApiListing apiListing, com.mangofactory.swagger.models.dto.ApiDescription apiDescription) {
            if (!exists(apiListing, apiDescription)) {
                apiListing.getApis().add(apiDescription);
            }
        }
        
        private boolean exists(com.mangofactory.swagger.models.dto.ApiListing apiListing, com.mangofactory.swagger.models.dto.ApiDescription apiDescription) {
            // TODO Auto-generated method stub
            return false;
        }

        private com.mangofactory.swagger.models.dto.ApiDescription getAll() {
            com.mangofactory.swagger.models.dto.Operation operation = 
                    newOperation().method(RequestMethod.GET.name())
                                  .responseClass("Iterable«" + information.getResultType().getSimpleName() + "»")
                                  .build();
            
            return buildApi(information.getBasePath(), "Get all entities", operation);
        }
        
        private com.mangofactory.swagger.models.dto.ApiDescription getOne() {
            com.mangofactory.swagger.models.dto.Operation operation = 
                    newOperation().method(RequestMethod.GET.name())
                                  .responseClass(information.getResultType().getSimpleName())
                                  .parameters(Arrays.asList(newIdParameter()))
                                  .build();
            
            return buildApi(information.getBasePath() + "/{id}", "Get one entity", operation);
        }
        
        private com.mangofactory.swagger.models.dto.ApiDescription create() {            
            com.mangofactory.swagger.models.dto.Operation operation = 
                    newOperation().method(RequestMethod.POST.name())
                                  .responseClass(information.getResultType().getSimpleName())
                                  .parameters(Arrays.asList(newBodyParameter(information.getCreateType())))
                                  .build();
            
            return buildApi(information.getBasePath(), "Create an entity", operation);
        }

        private com.mangofactory.swagger.models.dto.ApiDescription update() {
            com.mangofactory.swagger.models.dto.Operation operation = 
                    newOperation().method(RequestMethod.PUT.name())
                                  .responseClass(information.getResultType().getSimpleName())
                                  .parameters(Arrays.asList(newIdParameter(), newBodyParameter(information.getUpdateType())))
                                  .build();
            
            return buildApi(information.getBasePath() + "/{id}", "Updates an entity", operation);
        }
        
        private com.mangofactory.swagger.models.dto.ApiDescription delete() {
            com.mangofactory.swagger.models.dto.Operation operation = 
                    newOperation().method(RequestMethod.DELETE.name())
                                  .responseClass(Void.class.getSimpleName().toLowerCase())
                                  .parameters(Arrays.asList(newIdParameter()))
                                  .build();
            
            return buildApi(information.getBasePath() + "/{id}", "Deletes an entity", operation);
        }
        
        private com.mangofactory.swagger.models.dto.ApiDescription buildApi(String path, String description, com.mangofactory.swagger.models.dto.Operation operation) {
            return new com.mangofactory.swagger.models.dto.builder.ApiDescriptionBuilder()
                          .path(path)
                          .description(description)
                          .operations(Arrays.asList(operation))
                          .build();
        }
        
        private com.mangofactory.swagger.models.dto.builder.OperationBuilder newOperation() {
            return new com.mangofactory.swagger.models.dto.builder.OperationBuilder()
                          .authorizations(new ArrayList<com.mangofactory.swagger.models.dto.Authorization>());
        }

        private com.mangofactory.swagger.models.dto.builder.ParameterBuilder newParameter() {
            return new com.mangofactory.swagger.models.dto.builder.ParameterBuilder();
        }

        private com.mangofactory.swagger.models.dto.Parameter newIdParameter() {
            return newParameter()
                    .dataType(information.getIdentifierClass().getSimpleName().toLowerCase())
                    .name("id")
                    .parameterType("path")
                    .build();
        }

        private com.mangofactory.swagger.models.dto.Parameter newBodyParameter(Class<?> bodyClass) {
            return newParameter()
                    .dataType(bodyClass.getSimpleName())
                    .name("body")
                    .parameterType("body")
                    .build();
        }
        
    }

}

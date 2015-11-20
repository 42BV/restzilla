/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.handler;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import io.beanmapper.BeanMapper;
import io.beanmapper.core.rule.MappableFields;
import io.beanmapper.spring.Lazy;
import io.beanmapper.spring.PageableMapper;
import io.beanmapper.spring.util.JsonUtil;
import io.restzilla.RestConfig;
import io.restzilla.RestInformation;
import io.restzilla.RestInformation.ResultInformation;
import io.restzilla.handler.security.SecurityProvider;
import io.restzilla.handler.swagger.SwaggerApiDescriptor;
import io.restzilla.service.CrudService;
import io.restzilla.service.Listable;
import io.restzilla.service.impl.ReadService;
import io.restzilla.service.impl.ReadServiceListableAdapter;
import io.restzilla.util.PageableResolver;
import io.restzilla.util.UrlUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;

/**
 * Default implementation of the {@link EntityHandlerMappingFactory}.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class DefaultHandlerMappingFactory implements EntityHandlerMappingFactory {
    
    private final ObjectMapper objectMapper;
    
    private final ConversionService conversionService;
    
    private final BeanMapper beanMapper;
    
    private final ReadService readService;
    
    private final SecurityProvider securityProvider;
    
    /**
     * Instantiate a new {@link DefaultHandlerMappingFactory}.
     * 
     * @param objectMapper
     *              the {@link ObjectMapper} for JSON parsing and formatting
     * @param conversionService
     *              the {@link ConversionService} for converting between types
     * @param beanMapper
     *              the {@link BeanMapper} for mapping between beans
     * @param readService
     *              the {@link ReadService} for querying result entities
     * @param securityProvider
     *              the {@link SecurityProvider} checking the authorization
     */
    public DefaultHandlerMappingFactory(ObjectMapper objectMapper,
                                   ConversionService conversionService,
                                          BeanMapper beanMapper,
                                         ReadService readService,
                                    SecurityProvider securityProvider) {
        this.objectMapper = objectMapper;
        this.conversionService = conversionService;
        this.beanMapper = beanMapper;
        this.readService = readService;
        this.securityProvider = securityProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultHandlerMapping build(CrudService<?, ?> service, RestInformation information) {
        return new DefaultHandlerMapping(new DefaultCrudController(service, information));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private class DefaultCrudController {

        private final CrudService entityService;
        
        private final RestInformation information;
        
        public DefaultCrudController(CrudService entityService, RestInformation information) {
            this.entityService = entityService;
            this.information = information;
        }
        
        /**
         * Retrieve all entities.
         * 
         * @return the entities, in result type
         */
        @ResponseBody
        public Object findAll(HttpServletRequest request) {
            checkIsAuthorized(information.findAll().secured(), request);
            Listable<?> retriever = resolveEntityRetriever();
            if (information.isPagedOnly() || PageableResolver.isSupported(request)) {
                return findAllAsPage(retriever, request);
            } else {
                return findAllAsCollection(retriever, request);
            }
        }

        private void checkIsAuthorized(String[] roles, HttpServletRequest request) {
            if (!securityProvider.isAuthorized(roles, request)) {
                throw new SecurityException("Not authorized, should be one of: " + StringUtils.join(roles, ", "));
            }
        }

        private Listable<?> resolveEntityRetriever() {
            Listable<?> retrievable = entityService;
            ResultInformation result = information.getResultInfo(information.findAll());
            if (result.isQuery()) {
                retrievable = new ReadServiceListableAdapter(readService, result.getType());
            }
            return retrievable;
        }

        /**
         * Retrieve all entities in a page.
         * 
         * @param retriever the entity retriever
         * @param request the request
         * @return the page of entities, in result type
         */
        private Page findAllAsPage(Listable<?> retriever, HttpServletRequest request) {
            Pageable pageable = PageableResolver.getPageable(request, information.getEntityClass());
            Page<?> page = retriever.findAll(pageable);
            Class<?> resultType = information.getResultInfo(information.findAll()).getType();
            return PageableMapper.map(page, resultType, beanMapper);
        }

        /**
         * Retrieve all entities in a collection.
         * 
         * @param retriever the entity retriever
         * @return the collection of entities, in result type
         */
        private Collection findAllAsCollection(Listable<?> retriever, HttpServletRequest request) {
            Sort sort = PageableResolver.getSort(request, information.getEntityClass());
            List<?> entities = retriever.findAll(sort);
            Class<?> resultType = information.getResultInfo(information.findAll()).getType();
            return beanMapper.map(entities, resultType);
        }

        /**
         * Retrieve a single entity by identifier: /{id}
         * 
         * @param id the identifier
         * @return the entity, in result type
         */
        @ResponseBody
        public Object findOne(HttpServletRequest request) {
            checkIsAuthorized(information.findOne().secured(), request);
            return mapIdToResult(extractId(request));
        }
        
        private Serializable extractId(HttpServletRequest request) {
            String path = UrlUtils.getPath(request);
            String raw = StringUtils.substringAfterLast(path, UrlUtils.SLASH);
            return conversionService.convert(raw, information.getIdentifierClass().asSubclass(Serializable.class));
        }

        private Object mapIdToResult(Serializable id) {
            ResultInformation result = information.getResultInfo(information.findOne());
            if (result.isQuery()) {
                return readService.getOne((Class) result.getType(), id);
            } else {
                return beanMapper.map(entityService.getOne(id), result.getType());
            }
        }

        //
        // Modifications
        //

        /**
         * Creates a new entity. Any content is retrieved from the request body.
         * 
         * @return the entity
         */
        @ResponseBody
        public Object create(HttpServletRequest request) throws Exception {
            checkIsAuthorized(information.create().secured(), request);
            Object input = objectMapper.readValue(request.getReader(), information.getInputType(information.create()));
            Persistable<?> entity = beanMapper.map(input, information.getEntityClass());
            Persistable<?> output = entityService.save(entity);
            return mapEntityToResult(output, information.create());
        }
        
        /**
         * Updates an entity. Any content is retrieved from the request body.
         * 
         * @return the updated entity
         */
        @ResponseBody
        public Object update(HttpServletRequest request) throws Exception {
            checkIsAuthorized(information.update().secured(), request);
            Serializable id = extractId(request);
            String json = CharStreams.toString(request.getReader());
            Object input = objectMapper.readValue(json, information.getInputType(information.update()));
            Persistable<?> output = entityService.save(new LazyMappingEntity(id, input, json));
            return mapEntityToResult(output, information.update());
        }

        /**
         * Deletes an entity based on an identifier: /{id}
         * 
         * @return the deleted entity
         */
        @ResponseBody
        public Object delete(HttpServletRequest request) {
            checkIsAuthorized(information.delete().secured(), request);
            Persistable<?> entity = entityService.getOne(extractId(request));
            entityService.delete(entity);
            return mapEntityToResult(entity, information.delete());
        }
        
        /**
         * Converst the entity into our desired result type.
         * 
         * @param entity the entity
         * @param config the configuration
         * @return the entity in its result type
         */
        private Object mapEntityToResult(Persistable<?> entity, RestConfig config) {
            ResultInformation result = information.getResultInfo(config);
            if (result.isQuery()) {
                return readService.getOne((Class) result.getType(), entity.getId());
            } else {
                return convertToType(entity, result.getType());
            }
        }

        /**
         * Enhances our bean mapper with some common non-bean types that can be returned. 
         * 
         * @param entity the entity
         * @param resultType the result type
         * @return the converted object
         */
        private Object convertToType(Persistable<?> entity, Class<?> resultType) {
            if (Void.class.equals(resultType)) {
                return null;
            } else if (information.getIdentifierClass().equals(resultType)) {
                return entity.getId();
            } else {
                return beanMapper.map(entity, resultType);
            }
        }

        /**
         * Maps our input into the persisted entity on demand.
         * This mapping is performed lazy to ensure that the
         * entity is not auto-updated between transactions.
         *
         * @author Jeroen van Schagen
         * @since Nov 13, 2015
         */
        private class LazyMappingEntity implements Lazy<Object> {
            
            private final Serializable id;
            
            private final Object input;
            
            private final String json;
            
            public LazyMappingEntity(Serializable id, Object input, String json) {
                this.id = id;
                this.input = input;
                this.json = json;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public Object get() {
                Persistable<?> entity = entityService.getOne(id);
                if (information.isPatch()) {
                    Set<String> propertyNames = JsonUtil.getPropertyNamesFromJson(json, objectMapper);
                    beanMapper.map(input, entity, new MappableFields(propertyNames));
                } else {
                    beanMapper.map(input, entity);
                }
                return entity;
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
        
        private static final String FIND_ALL_NAME = "findAll";
        private static final String FIND_ONE_NAME = "findOne";
        private static final String CREATE_NAME = "create";
        private static final String UPDATE_NAME = "update";
        private static final String DELETE_NAME = "delete";

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
            if (getInformation().isReadOnly() && !hasRequestMethod(request, GET)) {
                return null;
            }

            Method method = null;
            int fragments = UrlUtils.getPath(request).split(UrlUtils.SLASH).length;
            if (fragments == 1) {
                if (hasRequestMethod(request, GET)) {
                    method = toMethodIfEnabled(FIND_ALL_NAME, getInformation().findAll());
                } else if (hasRequestMethod(request, POST)) {
                    method = toMethodIfEnabled(CREATE_NAME, getInformation().create());
                }
            } else if (fragments == 2) {
                if (hasRequestMethod(request, GET)) {
                    method = toMethodIfEnabled(FIND_ONE_NAME, getInformation().findOne());
                } else if (hasRequestMethod(request, PUT)) {
                    method = toMethodIfEnabled(UPDATE_NAME, getInformation().update());
                } else if (hasRequestMethod(request, DELETE)) {
                    method = toMethodIfEnabled(DELETE_NAME, getInformation().delete());
                }
            }
            return method;
        }
        
        private boolean hasRequestMethod(HttpServletRequest request, RequestMethod method) {
            return method.name().equals(request.getMethod());
        }

        private Method toMethodIfEnabled(String methodName, RestConfig config) throws NoSuchMethodException {
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
        
        private static final String FIND_ALL_NAME = "findAll";
        private static final String FIND_ONE_NAME = "findOne";
        private static final String CREATE_NAME = "create";
        private static final String UPDATE_NAME = "update";
        private static final String DELETE_NAME = "delete";
        
        private static final String ID_PARAM = "id";
        private static final String PAGE_PARAM = "page";
        private static final String SIZE_PARAM = "size";
        private static final String SORT_PARAM = "sort";
        
        private final com.mangofactory.swagger.models.ModelProvider modelProvider;
        
        private final RestInformation information;
        
        private final String basePath;

        DefaultSwaggerDescriber(com.mangofactory.swagger.models.ModelProvider modelProvider, RestInformation information) {
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
         * 
         * @param listing the API listings to enhance
         */
        void enhance(com.mangofactory.swagger.models.dto.ApiListing listing) {
            registerFindAll(listing);
            registerFindOne(listing);
            if (!information.isReadOnly()) {
                registerCreate(listing);
                registerUpdate(listing);
                registerDelete(listing);
            }
        }
        
        private void registerFindAll(com.mangofactory.swagger.models.dto.ApiListing listing) {
            if (information.findAll().enabled()) {
                addModel(listing, information.getResultType(information.findAll()));
                newDescription(FIND_ALL_NAME, basePath, RequestMethod.GET)
                        .responseClassIterable(information.getResultType(information.findAll()))
                        .addQueryParameter(PAGE_PARAM, Long.class, false)
                        .addQueryParameter(SIZE_PARAM, Long.class, false)
                        .addQueryParameter(SORT_PARAM, String.class, false)
                        .register(listing);
            }
        }
        
        private void registerFindOne(com.mangofactory.swagger.models.dto.ApiListing listing) {
            if (information.findOne().enabled()) {
                addModel(listing, information.getResultType(information.findOne()));
                newDescription(FIND_ONE_NAME, basePath + "/{id}", RequestMethod.GET)
                    .responseClass(information.getResultType(information.findOne()))
                    .addPathParameter(ID_PARAM, information.getIdentifierClass())
                    .register(listing);
            }
        }
        
        private void registerCreate(com.mangofactory.swagger.models.dto.ApiListing listing) {
            if (information.create().enabled()) {
                addModel(listing, information.getInputType(information.create()));
                addModel(listing, information.getResultType(information.create()));
                newDescription(CREATE_NAME, basePath, RequestMethod.POST)
                    .responseClass(information.getResultType(information.create()))
                    .addBodyParameter(information.getInputType(information.create()))
                    .register(listing);
            }
        }

        private void registerUpdate(com.mangofactory.swagger.models.dto.ApiListing listing) {
            if (information.update().enabled()) {
                addModel(listing, information.getInputType(information.update()));
                addModel(listing, information.getResultType(information.update()));
                newDescription(UPDATE_NAME, basePath + "/{id}", RequestMethod.PUT)
                    .responseClass(information.getResultType(information.update()))
                    .addPathParameter(ID_PARAM, information.getIdentifierClass())
                    .addBodyParameter(information.getInputType(information.update()))
                    .register(listing);
            }
        }

        private void registerDelete(com.mangofactory.swagger.models.dto.ApiListing listing) {
            if (information.delete().enabled()) {
                addModel(listing, information.getResultType(information.delete()));
                newDescription(DELETE_NAME, basePath + "/{id}", RequestMethod.DELETE)
                    .responseClass(information.getResultType(information.delete()))
                    .addPathParameter(ID_PARAM, information.getIdentifierClass())
                    .register(listing);
            }
        }

        private void addModel(com.mangofactory.swagger.models.dto.ApiListing listing, Class<?> modelType) {
            io.restzilla.handler.swagger.SwaggerUtils.addIfNotExists(listing, modelType, modelProvider);
        }
        
        private io.restzilla.handler.swagger.SwaggerUtils.DescriptionBuilder newDescription(String description, String path, RequestMethod method) {
            return io.restzilla.handler.swagger.SwaggerUtils.newDescription(description, path, method);
        }

    }
    
}

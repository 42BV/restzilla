/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.flyweight.handler;

import static io.flyweight.RestMappingStrategy.QUERY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import io.beanmapper.BeanMapper;
import io.beanmapper.core.rule.SourceFieldMapperRule;
import io.flyweight.RestConfig;
import io.flyweight.RestInformation;
import io.flyweight.handler.security.SecurityProvider;
import io.flyweight.handler.swagger.SwaggerApiDescriptor;
import io.flyweight.service.CrudService;
import io.flyweight.service.Retriever;
import io.flyweight.service.impl.ReadService;
import io.flyweight.util.PageableResolver;
import io.flyweight.util.UrlUtils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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

        private final CrudService service;
        
        private final RestInformation information;
        
        public DefaultCrudController(CrudService service, RestInformation information) {
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
            checkIsAuthorized(information.findAll().secured(), request);
            Retriever<?> retriever = resolveEntityRetriever();
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

        private Retriever<?> resolveEntityRetriever() {
            Retriever<?> retrievable = service;
            if (information.findAll().strategy().equals(QUERY)) {
                retrievable = new WrappedReadService(information.findAll().resultType());
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
        private Page findAllAsPage(Retriever<?> retriever, HttpServletRequest request) {
            Pageable pageable = PageableResolver.getPageable(request, information.getEntityClass());
            Page<?> result = retriever.findAll(pageable);
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
         * @param retriever the entity retriever
         * @return the collection of entities, in result type
         */
        private Collection findAllAsCollection(Retriever<?> retriever, HttpServletRequest request) {
            Sort sort = PageableResolver.getSort(request, information.getEntityClass());
            List<?> entities = retriever.findAll(sort);
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
            checkIsAuthorized(information.findOne().secured(), request);
            Serializable id = extractId(request);
            Class<?> resultType = information.getResultType(information.findOne());
            if (information.findOne().strategy().equals(QUERY)) {
                return readService.getOne(resultType, id);
            } else {
                return beanMapper.map(service.getOne(id), resultType);
            }
        }
        
        private Serializable extractId(HttpServletRequest request) {
            String path = UrlUtils.getPath(request);
            String raw = StringUtils.substringAfterLast(path, UrlUtils.SLASH);
            return conversionService.convert(raw, information.getIdentifierClass().asSubclass(Serializable.class));
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
            Persistable<?> output = service.save(entity);
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
            String json = CharStreams.toString(request.getReader());
            Object input = objectMapper.readValue(json, information.getInputType(information.update()));
            Persistable<?> entity = service.getOne(extractId(request));
            mapInputToEntity(input, entity, json);
            Persistable<?> output = service.save(entity);
            return mapEntityToResult(output, information.update());
        }
        
        private void mapInputToEntity(Object input, Persistable<?> entity, String json) throws JsonProcessingException, IOException {
            if (information.update().patch()) {
                Set<String> propertyNames = getPropertyNamesFromJson(json);
                beanMapper.map(input, entity, new SourceFieldMapperRule(propertyNames));
            } else {
                beanMapper.map(input, entity);
            }
        }
        
        private Set<String> getPropertyNamesFromJson(String json) throws JsonProcessingException, IOException {
            JsonNode tree = objectMapper.readTree(json);
            return getPropertyNames(tree, "");
        }
        
        private Set<String> getPropertyNames(JsonNode node, String base) {
            Set<String> propertyNames = new HashSet<String>();
            Iterator<String> iterator = node.fieldNames();
            while (iterator.hasNext()) {
                String fieldName = iterator.next();
                String propertyName = isNotBlank(base) ? base + "." + fieldName : fieldName;
                propertyNames.add(propertyName);
                propertyNames.addAll(getPropertyNames(node.get(fieldName), propertyName));
            }
            return propertyNames;
        }

        /**
         * Deletes an entity based on an identifier: /{id}
         * 
         * @return the deleted entity
         */
        @ResponseBody
        public Object delete(HttpServletRequest request) {
            checkIsAuthorized(information.delete().secured(), request);
            Persistable<?> entity = service.getOne(extractId(request));
            service.delete(entity);
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
            if (config.strategy().equals(QUERY)) {
                return readService.getOne(config.resultType(), entity.getId());
            } else {
                return convertToType(entity, information.getResultType(config));
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

    }
    
    /**
     * Create a read service wrapper that attaches us to a specific entity class.
     *
     * @author Jeroen van Schagen
     * @since Nov 6, 2015
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private class WrappedReadService implements Retriever<Object> {
        
        private final Class entityClass;
        
        public WrappedReadService(Class entityClass) {
            this.entityClass = entityClass;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<Object> findAll(Sort sort) {
            return readService.findAll(entityClass, sort);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Page<Object> findAll(Pageable pageable) {
            return readService.findAll(entityClass, pageable);
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
            if (fragments == 2) {
                if (hasRequestMethod(request, GET)) {
                    method = toMethodIfEnabled(FIND_ALL_NAME, getInformation().findAll());
                } else if (hasRequestMethod(request, POST)) {
                    method = toMethodIfEnabled(CREATE_NAME, getInformation().create());
                }
            } else if (fragments == 3) {
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
                newDescription(FIND_ALL_NAME, basePath, RequestMethod.GET).responseClassIterable(information.getResultType(information.findAll())).addQueryParameter(
                        PAGE_PARAM, Long.class, false).addQueryParameter(SIZE_PARAM, Long.class, false).addQueryParameter(SORT_PARAM, String.class, false).register(
                        listing);
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
            io.flyweight.handler.swagger.SwaggerUtils.addIfNotExists(listing, modelType, modelProvider);
        }
        
        private io.flyweight.handler.swagger.SwaggerUtils.DescriptionBuilder newDescription(String description, String path, RequestMethod method) {
            return io.flyweight.handler.swagger.SwaggerUtils.newDescription(description, path, method);
        }

    }
    
}

package io.restzilla.handler;

import static org.springframework.util.StringUtils.collectionToDelimitedString;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import io.beanmapper.BeanMapper;
import io.beanmapper.spring.Lazy;
import io.restzilla.RestConfig;
import io.restzilla.RestInformation;
import io.restzilla.RestInformation.QueryInformation;
import io.restzilla.RestInformation.ResultInformation;
import io.restzilla.handler.query.BeanMappingListable;
import io.restzilla.handler.query.CrudServiceListable;
import io.restzilla.handler.query.Finder;
import io.restzilla.handler.query.Listable;
import io.restzilla.handler.query.ReadServiceListable;
import io.restzilla.handler.query.RepositoryMethodListable;
import io.restzilla.handler.security.SecurityProvider;
import io.restzilla.handler.swagger.SwaggerApiDescriptor;
import io.restzilla.service.CrudService;
import io.restzilla.service.CrudServiceRegistry;
import io.restzilla.service.ReadService;
import io.restzilla.util.JsonUtil;
import io.restzilla.util.PageableResolver;
import io.restzilla.util.UrlUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;

/**
 * Default implementation of the {@link ResourceHandlerMappingFactory}.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class SimpleResourceHandlerMappingFactory implements ResourceHandlerMappingFactory {
    
    private static final String ARRAY_JSON_START = "[";

    private final ObjectMapper objectMapper;
    
    private final ConversionService conversionService;
    
    private final BeanMapper beanMapper;

    private final SecurityProvider securityProvider;
    
    private final Validator validator;
    
    private ReadService readService;

    private CrudServiceRegistry crudServiceRegistry;

    /**
     * Instantiate a new {@link SimpleResourceHandlerMappingFactory}.
     * 
     * @param objectMapper
     *              the {@link ObjectMapper} for JSON parsing and formatting
     * @param conversionService
     *              the {@link ConversionService} for converting between types
     * @param beanMapper
     *              the {@link BeanMapper} for mapping between beans
     * @param securityProvider
     *              the {@link SecurityProvider} checking the authorization
     * @param validator
     *              the {@link Validator} for verifying the input
     */
    public SimpleResourceHandlerMappingFactory(ObjectMapper objectMapper,
                                          ConversionService conversionService,
                                                 BeanMapper beanMapper,
                                           SecurityProvider securityProvider,
                                                  Validator validator) {
        this.objectMapper = objectMapper;
        this.conversionService = conversionService;
        this.beanMapper = beanMapper;
        this.securityProvider = securityProvider;
        this.validator = validator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultHandlerMapping build(RestInformation information) {
        return new DefaultHandlerMapping(new DefaultCrudController(information));
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private class DefaultCrudController {
        
        private final RestInformation information;
        
        /**
         * Always access this field by {@code getEntityService()} as it is initialized lazy.
         */
        private CrudService entityService;
        
        public DefaultCrudController(RestInformation information) {
            this.information = information;
        }
        
        //
        // Read queries
        //

        /**
         * Retrieve all entities.
         * 
         * @return the entities, in result type
         */
        @ResponseBody
        public Object findAll(HttpServletRequest request) {
            QueryInformation query = information.findQuery(request.getParameterMap());
            Listable<?> listable = buildListable(query, request);

            if (query != null) {
                ensureIsReadable(query.getSecured(), request);
                if (query.isSingleResult()) {
                    return ((Finder) listable).findOne();
                }
            } else {
                ensureIsReadable(information.findAll().secured(), request);
            }
            
            return findAll(listable, request);
        }

        private void ensureIsReadable(String[] expressions, HttpServletRequest request) {
            if (!hasAnyNotBlank(expressions)) {
                expressions = information.getReadSecured();
            }
            ensureIsAuthorized(expressions, request);
        }

        private boolean hasAnyNotBlank(String[] expressions) {
            boolean result = false;
            for (String expression : expressions) {
                if (StringUtils.isNotBlank(expression)) {
                    result = true;
                }
            }
            return result;
        }

        private void ensureIsAuthorized(String[] expressions, HttpServletRequest request) {
            if (!securityProvider.isAuthorized(expressions, request)) {
                throw new SecurityException("Not authorized, should be one of: " + StringUtils.join(expressions, ", "));
            }
        }

        private Listable<?> buildListable(QueryInformation query, HttpServletRequest request) {
            Listable<?> delegate = new CrudServiceListable(entityService, information.getEntityClass());

            ResultInformation result = information.getResultInfo(information.findAll());
            Class<?> resultType = result.getType();

            if (query != null) {
                delegate = new RepositoryMethodListable(crudServiceRegistry, conversionService, information, query, request.getParameterMap());
                resultType = query.getResultType();
            } else if (result.isByQuery()) {
                return new ReadServiceListable(readService, resultType);
            }

            return new BeanMappingListable(delegate, beanMapper, resultType);
        }

        private Object findAll(Listable<?> listable, HttpServletRequest request) {
            Sort sort = PageableResolver.getSort(request, listable.getEntityClass());
            if (information.isPagedOnly() || PageableResolver.isSupported(request)) {
                Pageable pageable = PageableResolver.getPageable(request, sort);
                return listable.findAll(pageable);
            } else {
                return listable.findAll(sort);
            }
        }

        /**
         * Retrieve a single entity by identifier: /{id}
         * 
         * @param id the identifier
         * @return the entity, in result type
         */
        @ResponseBody
        public Object findOne(HttpServletRequest request) {
            ensureIsReadable(information.findOne().secured(), request);
            return mapIdToResult(extractId(request));
        }
        
        private Serializable extractId(HttpServletRequest request) {
            String path = UrlUtils.getPath(request);
            String raw = StringUtils.substringAfterLast(path, UrlUtils.SLASH);
            return conversionService.convert(raw, information.getIdentifierClass().asSubclass(Serializable.class));
        }

        private Object mapIdToResult(Serializable id) {
            ResultInformation result = information.getResultInfo(information.findOne());
            if (result.isByQuery()) {
                return readService.getOne((Class) result.getType(), id);
            } else {
                return beanMapper.map(entityService.getOne(id), result.getType());
            }
        }

        //
        // Modifications
        //

        /**
         * Saves an entity. Any content is retrieved from the request body.
         * 
         * @return the entity
         */
        @ResponseBody
        public Object create(HttpServletRequest request) throws Exception {
            ensureIsModifiable(information.create().secured(), request);
            String json = CharStreams.toString(request.getReader()).trim();
            Class<?> inputType = information.getInputType(information.create());

            if (json.startsWith(ARRAY_JSON_START)) {
                List<Object> inputs = objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, inputType));
                List<Object> results = new ArrayList<Object>();
                for (Object input : inputs) {
                    results.add(doCreate(input, json));
                }
                return results;
            } else {
                Object input = objectMapper.readValue(json, inputType);
                return doCreate(input, json);
            }
        }
        
        private void ensureIsModifiable(String[] expressions, HttpServletRequest request) {
            if (!hasAnyNotBlank(expressions)) {
                expressions = information.getModifySecured();
            }
            ensureIsAuthorized(expressions, request);
        }
        
        private Object doCreate(Object input, String json) throws BindException {
            Persistable<?> entity = beanMapper.map(validate(input), information.getEntityClass());
            Persistable<?> output = entityService.save(entity);
            return mapToResult(output, information.create());
        }

        private Object validate(Object input) throws BindException {
            BeanPropertyBindingResult errors = new BeanPropertyBindingResult(input, "input");
            validator.validate(input, errors);
            if (errors.hasErrors()) {
                throw new BindException(errors);
            }
            return input;
        }

        /**
         * Updates an entity. Any content is retrieved from the request body.
         * 
         * @return the updated entity
         */
        @ResponseBody
        public Object update(HttpServletRequest request) throws Exception {
            Serializable id = extractId(request);
            String json = CharStreams.toString(request.getReader());
            Object input = objectMapper.readValue(json, information.getInputType(information.update()));
            return doUpdate(id, json, validate(input));
        }
        
        private Object doUpdate(Serializable id, String json, Object input) throws BindException {
            Persistable<?> output = entityService.save(new LazyMergingEntity(id, input, json));
            return mapToResult(output, information.update());
        }

        /**
         * Deletes an entity based on an identifier: /{id}
         * 
         * @return the deleted entity
         */
        @ResponseBody
        public Object delete(HttpServletRequest request) {
            ensureIsModifiable(information.delete().secured(), request);
            Persistable<?> entity = entityService.getOne(extractId(request));
            entityService.delete(entity);
            return mapToResult(entity, information.delete());
        }
        
        /**
         * Converst the entity into our desired result type.
         * 
         * @param entity the entity
         * @param config the configuration
         * @return the entity in its result type
         */
        private Object mapToResult(Persistable<?> entity, RestConfig config) {
            ResultInformation result = information.getResultInfo(config);
            if (result.isByQuery()) {
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
            if (entity == null || Void.class.equals(resultType)) {
                return null;
            } else if (entity.getClass().equals(resultType)) {
                return entity;
            } else if (information.getIdentifierClass().equals(resultType)) {
                return entity.getId();
            } else {
                return beanMapper.map(entity, resultType);
            }
        }
        
        private void init() {
            if (entityService == null) {
                entityService = crudServiceRegistry.getService((Class) information.getEntityClass());
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
        private class LazyMergingEntity implements Lazy<Object> {
            
            private final Serializable id;
            
            private final Object input;
            
            private final String json;
            
            public LazyMergingEntity(Serializable id, Object input, String json) {
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
                    beanMapper.wrapConfig()
                            .downsizeSource(new ArrayList<String>(propertyNames))
                            .build()
                            .map(input, entity);
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
    private static class DefaultHandlerMapping extends ResourceHandlerMapping implements SwaggerApiDescriptor {
        
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
            controller.init(); // Lazy initialization

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
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void describe(Logger logger) {
            final String basePath = controller.information.getBasePath();
            
            logIf(controller.information.findAll(), logger, "Mapped \"[/{}],methods=[GET],params=[]\"", basePath);
            for (QueryInformation query : controller.information.getQueries()) {
                List<String> parameters = query.getRawParameters();
                logger.info("Mapped \"[/{}],methods=[GET],params=[{}]\"", basePath, collectionToDelimitedString(parameters, ","));
            }
            logIf(controller.information.findOne(), logger, "Mapped \"[/{}/{id}],methods=[GET],params=[]\"", basePath);
            
            if (!controller.information.isReadOnly()) {
                logIf(controller.information.create(), logger, "Mapped \"[/{}],methods=[POST],params=[]\"", basePath);
                logIf(controller.information.update(), logger, "Mapped \"[/{}/{id}],methods=[PUT],params=[]\"", basePath);
                logIf(controller.information.delete(), logger, "Mapped \"[/{}/{id}],methods=[DELETE],params=[]\"", basePath);
            }
        }
        
        private void logIf(RestConfig config, Logger logger, String msg, Object... args) {
            if (config.enabled()) {
                logger.info(msg, args);
            }
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
    
    @Autowired
    public void setReadService(ReadService readService) {
        this.readService = readService;
    }
    
    @Autowired
    public void setCrudServiceRegistry(CrudServiceRegistry crudServiceRegistry) {
        this.crudServiceRegistry = crudServiceRegistry;
    }

}

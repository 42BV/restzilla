/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify;

import io.beanmapper.BeanMapper;
import io.restify.handler.CrudHandlerMappingFactory;
import io.restify.handler.DefaultCrudHandlerMappingFactory;
import io.restify.handler.PublicHandlerMapping;
import io.restify.handler.RootCrudHandlerMapping;
import io.restify.service.CrudService;
import io.restify.service.CrudServiceLocator;
import io.restify.service.CrudServiceRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Create a REST endpoint for all entities annotated with {@link EnableRest}.
 * This endpoint will provide full CRUD functionality on the entity,
 * following the conventional layered architecture: controller, service,
 * repository. At each layer in the architecture you are able to overwrite
 * the behaviour with a custom implementation. Otherwise, the default
 * implementation is injected.
 * 
 * <br><br>
 * 
 * <b>For usage, just inject this factory bean to the application context.</b>
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class CrudHandlerMappingFactoryBean implements FactoryBean<HandlerMapping>, InitializingBean, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrudHandlerMappingFactoryBean.class);

    /**
     * Request handler mapping.
     */
    private RequestMappingHandlerMapping requestHandlerMapping;
    
    // Service locator
    
    /**
     * Locates or creates CRUD services and repositories.
     */
    private CrudServiceLocator serviceLocator;
    
    /**
     * Application context used for locating and creating beans dynamically.
     */
    private ApplicationContext applicationContext;
    
    /**
     * Base package of the entities to scan.
     */
    private String basePackage;

    // Handler mapping

    /**
     * Creates the REST endpoint mappings.
     */
    private CrudHandlerMappingFactory handlerMappingFactory;
    
    /**
     * Maps between entities.
     */
    private BeanMapper beanMapper = new BeanMapper();
    
    /**
     * Converts the standard types.
     */
    private ConversionService conversionService = new DefaultConversionService();

    /**
     * Performs JSON marshall and unmarshalling.
     */
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public HandlerMapping getObject() throws Exception {
        RootCrudHandlerMapping rootHandler = new RootCrudHandlerMapping(requestHandlerMapping);
        CrudServiceRegistry services = serviceLocator.execute();
        for (Class<?> entityClass : services.getEntityClasses()) {
            EnableRest annotation = entityClass.getAnnotationsByType(EnableRest.class)[0];
            EntityInformation information = new EntityInformation(entityClass, annotation);

            CrudService<?, ?> service = services.getService(entityClass);
            PublicHandlerMapping handler = handlerMappingFactory.build(service, information);
            rootHandler.registerHandler(information.getBasePath(), handler);
            
            LOGGER.info("Generated REST mapping for /{} [{}]", information.getBasePath(), entityClass.getName());
        }
        return rootHandler;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getObjectType() {
        return HandlerMapping.class;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Instantiates the default service locator and handler
     * mapping factory, when left unconfigured.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (serviceLocator == null) {
            serviceLocator = new CrudServiceLocator(applicationContext, basePackage);
        }
        if (handlerMappingFactory == null) {
            handlerMappingFactory = new DefaultCrudHandlerMappingFactory(objectMapper, conversionService, beanMapper);
        }
    }

    @Autowired(required = false)
    public void setRequestHandlerMapping(RequestMappingHandlerMapping requestHandlerMapping) {
        this.requestHandlerMapping = requestHandlerMapping;
    }
    
    // Service locator
    
    /**
     * <i>Optionally</i> set a custom service locator.
     * @param serviceLocator the service locator
     */
    public void setServiceLocator(CrudServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    /**
     * Configure the base package of our entities.
     * @param basePackage the base package
     */
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }
    
    /**
     * Configure the base package class of our entities.
     * @param basePackageClass the base package class
     */
    public void setBasePackageClass(Class<?> basePackageClass) {
        setBasePackage(basePackageClass.getPackage().getName());
    }

    // Handler mapping
    
    /**
     * <i>Optionally</i> set a custom handler mapping factory.
     * @param handlerMappingFactory the handler mapping factory
     */
    public void setHandlerMappingFactory(CrudHandlerMappingFactory handlerMappingFactory) {
        this.handlerMappingFactory = handlerMappingFactory;
    }
    
    @Autowired(required = false)
    public void setBeanMapper(BeanMapper beanMapper) {
        this.beanMapper = beanMapper;
    }
    
    @Autowired(required = false)
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    @Autowired(required = false)
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

}

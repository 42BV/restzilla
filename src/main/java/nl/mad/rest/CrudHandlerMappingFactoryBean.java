/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.rest;

import nl.mad.rest.handler.CrudHandlerMappingFactory;
import nl.mad.rest.handler.RootCrudHandlerMapping;
import nl.mad.rest.handler.DefaultCrudHandlerMappingFactory;
import nl.mad.rest.handler.PublicHandlerMapping;
import nl.mad.rest.service.CrudService;
import nl.mad.rest.service.CrudServiceLocator;
import nl.mad.rest.service.CrudServiceRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Create a REST endpoint for all entities marked with @EnableRest.
 * This endpoint will provide full CRUD functionality on the entity,
 * following the conventional layered architecture: controller, service,
 * repository. At each layer in the architecture you are able to overwrite
 * the behaviour with a custom implementation. Otherwise, the default
 * implementation is injected.
 * 
 * <br/><br/>
 * <b>For usage, just inject this factory bean to the application context</b>:
 * <br/><br/>
 * 
 * <code>
 * public CrudHandlerMappingFactoryBean crudHandlerMapping() {
 *    CrudHandlerMappingFactoryBean factoryBean = new CrudHandlerMappingFactoryBean();
 *    factoryBean.setBasePackageClass(WebMvcConfig.class);
 *    return factoryBean;
 * }
 * </code>
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class CrudHandlerMappingFactoryBean implements FactoryBean<HandlerMapping>, InitializingBean, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrudHandlerMappingFactoryBean.class);

    /**
     * Application context used for locating and creating beans dynamically.
     */
    private ApplicationContext applicationContext;
    
    /**
     * Base package of the entities to scan.
     */
    private String basePackage;
    
    /**
     * Locates or creates CRUD services and repositories.
     */
    private CrudServiceLocator serviceLocator;

    /**
     * Creates the REST endpoint mappings.
     */
    private CrudHandlerMappingFactory handlerMappingFactory;
    
    @Autowired(required = false)
    private RequestMappingHandlerMapping requestHandlerMapping;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public HandlerMapping getObject() throws Exception {
        RootCrudHandlerMapping delegate = new RootCrudHandlerMapping(requestHandlerMapping);
        CrudServiceRegistry services = serviceLocator.execute();
        for (Class<?> entityClass : services.getEntityClasses()) {
            EnableRest annotation = entityClass.getAnnotationsByType(EnableRest.class)[0];
            EntityInformation information = new EntityInformation(entityClass, annotation);

            CrudService<?, ?> service = services.getService(entityClass);
            PublicHandlerMapping handlerMapping = handlerMappingFactory.build(service, information);
            delegate.register(information.getBasePath(), handlerMapping);
            
            LOGGER.info("Generated REST mapping for /{} [{}]", information.getBasePath(), entityClass.getName());
        }
        return delegate;
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
            ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper.class);
            ConversionService conversionService = applicationContext.getBean(ConversionService.class);
            handlerMappingFactory = new DefaultCrudHandlerMappingFactory(objectMapper, conversionService);
        }
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

    /**
     * <i>Optionally</i> set a custom service locator.
     * @param serviceLocator the service locator
     */
    public void setServiceLocator(CrudServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }
    
    /**
     * <i>Optionally</i> set a custom handler mapping factory.
     * @param handlerMappingFactory the handler mapping factory
     */
    public void setHandlerMappingFactory(CrudHandlerMappingFactory handlerMappingFactory) {
        this.handlerMappingFactory = handlerMappingFactory;
    }

}

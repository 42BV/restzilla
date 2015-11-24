/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.config;

import static org.apache.commons.lang3.StringUtils.isBlank;
import io.beanmapper.BeanMapper;
import io.restzilla.RestInformation;
import io.restzilla.RestResource;
import io.restzilla.handler.DefaultHandlerMappingFactory;
import io.restzilla.handler.EntityHandlerMappingFactory;
import io.restzilla.handler.RestHandlerMapping;
import io.restzilla.handler.naming.DefaultRestNamingStrategy;
import io.restzilla.handler.naming.RestNamingStrategy;
import io.restzilla.handler.security.AlwaysSecurityProvider;
import io.restzilla.handler.security.SecurityProvider;
import io.restzilla.service.CrudService;
import io.restzilla.service.CrudServiceFactory;
import io.restzilla.service.CrudServiceLocator;
import io.restzilla.service.CrudServiceRegistry;
import io.restzilla.service.ReadService;
import io.restzilla.service.impl.DefaultServiceFactory;
import io.restzilla.util.NoOpValidator;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ClassUtils;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.HandlerMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Create a REST endpoint for all entities annotated with {@link RestResource}.
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
public class RestHandlerMappingFactoryBean implements FactoryBean<HandlerMapping>, ApplicationContextAware {
    
    /**
     * Base class used to check if Spring Security is available on the classpath.
     */
    private static final String SPRING_SECURITY_PATH = "org.springframework.security.core.context.SecurityContext";

    /**
     * Application context used to retrieve and create beans.
     */
    private ApplicationContext applicationContext;
    
    /**
     * Generates the base paths per entity.
     */
    private RestNamingStrategy namingStrategy = new DefaultRestNamingStrategy();

    /**
     * Creates CRUD service and repository beans.
     */
    private CrudServiceFactory serviceFactory;
    
    /**
     * Base package of the entities to scan.
     */
    private String basePackage;

    /**
     * Maps between entities.
     */
    private BeanMapper beanMapper;
    
    /**
     * Converts the standard types.
     */
    private ConversionService conversionService = new DefaultConversionService();

    /**
     * Performs JSON marshall and unmarshalling.
     */
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Checks the authorization.
     */
    private SecurityProvider securityProvider;
    
    /**
     * Validator.
     */
    private Validator validator = new NoOpValidator();

    /**
     * Performs queries.
     */
    private ReadService readService = new ReadService();
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public RestHandlerMapping getObject() throws Exception {
        RestHandlerMapping rootHandlerMapping = new RestHandlerMapping(applicationContext);
        
        CrudServiceRegistry serviceRegistry = buildServiceRegistry();
        afterServiceRegistry(serviceRegistry);
        EntityHandlerMappingFactory handlerMappingFactory = buildHandlerMappingFactory(serviceRegistry);

        // Register handler mapping per entity type
        for (Class entityClass : serviceRegistry.getEntityClasses()) {
            RestInformation entityInfo = buildInformation(entityClass);
            CrudService serviceInstance = serviceRegistry.getService(entityClass);
            rootHandlerMapping.registerHandler(handlerMappingFactory.build(serviceInstance, entityInfo));
        }

        return rootHandlerMapping;
    }

    /**
     * Start scanning the classpath and retrieve a registry of
     * all found entity services.
     * @return a registry with all entities and their related service
     */
    private CrudServiceRegistry buildServiceRegistry() {
        CrudServiceLocator serviceLocator = new CrudServiceLocator(applicationContext);
        if (serviceFactory == null) {
            serviceFactory = new DefaultServiceFactory(applicationContext);
        }
        return serviceLocator.buildRegistry(basePackage, serviceFactory);
    }
    
    /**
     * Initialized all required dependencies using the service registry.
     * @param serviceRegistry the service registry
     */
    protected void afterServiceRegistry(CrudServiceRegistry serviceRegistry) {
        if (beanMapper == null) {
            beanMapper = new BeanMapper();
            beanMapper.addPackagePrefix(basePackage);
        }
        if (securityProvider == null) {
            securityProvider = buildSecurityProvider();
        }
        readService.setServiceRegistry(serviceRegistry);
    }
    
    private SecurityProvider buildSecurityProvider() {
        if (ClassUtils.isPresent(SPRING_SECURITY_PATH, getClass().getClassLoader())) {
            SecurityProvider securityProvider = new io.restzilla.handler.security.SpelSecurityProvider();
            applicationContext.getAutowireCapableBeanFactory().autowireBean(securityProvider);
            return securityProvider;
        } else {
            return new AlwaysSecurityProvider();
        }
    }
    
    /**
     * Build the entity handler mapping factory, responsible for creating all
     * handler mappings for each entity.
     * @param serviceRegistry the service registry
     * @return the created factory
     */
    protected EntityHandlerMappingFactory buildHandlerMappingFactory(CrudServiceRegistry serviceRegistry) {
        return new DefaultHandlerMappingFactory(objectMapper, conversionService, beanMapper, readService, securityProvider, validator);
    }
    
    /**
     * Retrieve the REST meta data of an entity.
     * @param entityClass the entity class
     * @return the entity REST meta data
     * @throws NoSuchMethodException
     */
    private RestInformation buildInformation(Class<?> entityClass) throws NoSuchMethodException {
        RestResource annotation = entityClass.getAnnotationsByType(RestResource.class)[0];
        String basePath = annotation.basePath();
        if (isBlank(basePath)) {
            basePath = namingStrategy.getBasePath(entityClass);
        }
        return new RestInformation(entityClass, basePath, annotation);
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
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    /**
     * <i>Optionally</i> configure the naming strategy.
     * @param namingStrategy the namingStrategy to set
     */
    @Autowired(required = false)
    public void setNamingStrategy(RestNamingStrategy namingStrategy) {
        this.namingStrategy = namingStrategy;
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
     * <i>Optionally</i> set a custom service factory.
     * @param serviceFactory the service factory
     */
    @Autowired(required = false)
    public void setServiceFactory(CrudServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    /**
     * <i>Optionally</i> set a custom bean mapper.
     * @param beanMapper the bean mapper
     */
    @Autowired(required = false)
    public void setBeanMapper(BeanMapper beanMapper) {
        this.beanMapper = beanMapper;
    }
    
    /**
     * <i>Optionally</i> set a custom conversion service.
     * @param conversionService the conversion service
     */
    @Autowired(required = false)
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    /**
     * <i>Optionally</i> set a custom object mapper.
     * @param objectMapper the object mapper
     */
    @Autowired(required = false)
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * <i>Optionally</i> set a custom security provider on our mappings.
     * @param securityProvider the security provider
     */
    @Autowired(required = false)
    public void setSecurityProvider(SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }
    
    /**
     * Set the validator that should be used to check the input.
     * @param validator the validator to set
     */
    @Autowired(required = false)
    public void setValidator(Validator validator) {
        this.validator = validator;
    }
    
    /**
     * Set the read service used for queries.
     * @param readService the readService to set
     */
    @Autowired(required = false)
    public void setReadService(ReadService readService) {
        this.readService = readService;
    }

}

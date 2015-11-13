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
import io.restzilla.service.DefaultServiceFactory;
import io.restzilla.service.impl.ReadService;
import io.restzilla.util.ClasspathChecker;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
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
public class RestHandlerMappingFactoryBean implements FactoryBean<HandlerMapping>, InitializingBean, ApplicationContextAware {
    
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

    // Service

    /**
     * Creates CRUD service and repository beans.
     */
    private CrudServiceFactory serviceFactory;
    
    /**
     * Base package of the entities to scan.
     */
    private String basePackage;
    
    // Controller

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
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public RestHandlerMapping getObject() throws Exception {
        RestHandlerMapping rootHandlerMapping = new RestHandlerMapping(applicationContext);
        
        // Scans the classpath and register all resources per entity
        CrudServiceLocator locator = new CrudServiceLocator(applicationContext);
        CrudServiceRegistry registry = locator.buildRegistry(basePackage, serviceFactory);
        
        // Build the default handler mapping factory for entities 
        EntityHandlerMappingFactory handlerMappingFactory = buildHandlerMappingFactory(registry);

        // Register the handler mapping per detected entity
        for (Class entityClass : registry.getEntityClasses()) {
            RestInformation entityInfo = buildInformation(entityClass);
            CrudService crudService = registry.getService(entityClass);
            rootHandlerMapping.registerHandler(handlerMappingFactory.build(crudService, entityInfo));
        }

        return rootHandlerMapping;
    }

    private RestInformation buildInformation(Class<?> entityClass) throws NoSuchMethodException {
        RestResource annotation = entityClass.getAnnotationsByType(RestResource.class)[0];
        String basePath = annotation.basePath();
        if (isBlank(basePath)) {
            basePath = namingStrategy.getBasePath(entityClass);
        }
        RestInformation information = new RestInformation(entityClass, basePath, annotation);
        return information;
    }
    
    protected EntityHandlerMappingFactory buildHandlerMappingFactory(CrudServiceRegistry registry) {
        return new DefaultHandlerMappingFactory(objectMapper, conversionService, beanMapper, new ReadService(registry), securityProvider);
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

    @Override
    public void afterPropertiesSet() throws Exception {
        if (beanMapper == null) {
            beanMapper = new BeanMapper();
            beanMapper.addPackagePrefix(basePackage);
        }
        if (serviceFactory == null) {
            serviceFactory = new DefaultServiceFactory(applicationContext);
        }
        if (securityProvider == null) {
            securityProvider = buildSecurityProvider();
        }
    }

    private SecurityProvider buildSecurityProvider() {
        if (ClasspathChecker.isOnClasspath(SPRING_SECURITY_PATH)) {
            SecurityProvider securityProvider = new io.restzilla.handler.security.SpelSecurityProvider();
            applicationContext.getAutowireCapableBeanFactory().autowireBean(securityProvider);
            return securityProvider;
        } else {
            return new AlwaysSecurityProvider();
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
     * <i>Optionally</i> configure the naming strategy.
     * @param namingStrategy the namingStrategy to set
     */
    public void setNamingStrategy(RestNamingStrategy namingStrategy) {
        this.namingStrategy = namingStrategy;
    }

    // Service locator

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

    // Handler mapping

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

}

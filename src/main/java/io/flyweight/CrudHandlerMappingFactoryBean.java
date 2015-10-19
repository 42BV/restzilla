/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.flyweight;

import io.beanmapper.BeanMapper;
import io.flyweight.handler.CrudHandlerMapping;
import io.flyweight.handler.DefaultHandlerMappingFactory;
import io.flyweight.handler.EntityHandlerMapping;
import io.flyweight.handler.EntityHandlerMappingFactory;
import io.flyweight.handler.security.AlwaysSecurityProvider;
import io.flyweight.handler.security.SecurityProvider;
import io.flyweight.service.CrudService;
import io.flyweight.service.CrudServiceLocator;
import io.flyweight.service.CrudServiceRegistry;
import io.flyweight.service.factory.CrudServiceFactory;
import io.flyweight.service.factory.DefaultServiceFactory;

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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Create a REST endpoint for all entities annotated with {@link RestEnable}.
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
     * Base class used to check if Spring Security is available on the classpath.
     */
    private static final String SPRING_SECURITY_PATH = "org.springframework.security.core.context.SecurityContext";

    /**
     * Application context used to retrieve and create beans.
     */
    private ApplicationContext applicationContext;

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
     * Creates REST endpoint mappings.
     */
    private EntityHandlerMappingFactory handlerMappingFactory;
    
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
     * Checks the authorization.
     */
    private SecurityProvider securityProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public CrudHandlerMapping getObject() throws Exception {
        CrudHandlerMapping handlerMapping = new CrudHandlerMapping(applicationContext);

        new CrudServiceLocator(applicationContext).registerAll(basePackage, serviceFactory);
        for (Class<?> entityClass : CrudServiceRegistry.getEntityClasses()) {
            RestEnable annotation = entityClass.getAnnotationsByType(RestEnable.class)[0];
            RestInformation information = new RestInformation(entityClass, annotation);

            CrudService service = CrudServiceRegistry.getService(entityClass);
            EntityHandlerMapping entityHandlerMapping = handlerMappingFactory.build(service, information);
            handlerMapping.registerHandler(information.getBasePath(), entityHandlerMapping);
            
            LOGGER.info("Generated REST mapping for /{} [{}]", information.getBasePath(), entityClass.getName());
        }
        return handlerMapping;
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
     * Instantiate beans when not configured.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (serviceFactory == null) {
            serviceFactory = new DefaultServiceFactory(applicationContext);
        }
        if (handlerMappingFactory == null) {
            if (securityProvider == null) {
                buildSecurityProvider();
            }
            handlerMappingFactory = new DefaultHandlerMappingFactory(objectMapper, conversionService, beanMapper, securityProvider);
        }
    }
    

    private void buildSecurityProvider() {
        try {
            Class.forName(SPRING_SECURITY_PATH);
            securityProvider = new io.flyweight.handler.security.SpelSecurityProvider();
            applicationContext.getAutowireCapableBeanFactory().autowireBean(securityProvider);
        } catch (ClassNotFoundException cnfe) {
            securityProvider = new AlwaysSecurityProvider();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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
     * <i>Optionally</i> set a custom handler mapping factory.
     * @param handlerMappingFactory the handler mapping factory
     */
    @Autowired(required = false)
    public void setHandlerMappingFactory(EntityHandlerMappingFactory handlerMappingFactory) {
        this.handlerMappingFactory = handlerMappingFactory;
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

}

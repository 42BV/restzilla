/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.beanmapper.BeanMapper;
import io.beanmapper.config.BeanMapperBuilder;
import io.beanmapper.utils.Classes;
import nl._42.restzilla.RestProperties;
import nl._42.restzilla.RestResource;
import nl._42.restzilla.web.DefaultHandlerMappingFactory;
import nl._42.restzilla.web.ResourceHandlerMapping;
import nl._42.restzilla.web.ResourceHandlerMappingFactory;
import nl._42.restzilla.web.RestHandlerMapping;
import nl._42.restzilla.web.security.AlwaysSecurityProvider;
import nl._42.restzilla.web.security.SecurityProvider;
import nl._42.restzilla.web.security.SpelSecurityProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.HandlerMapping;

import java.util.HashSet;
import java.util.Set;

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
class RestHandlerMappingFactoryBean implements FactoryBean<HandlerMapping>, ApplicationContextAware {
    
    /**
     * Base class used to check if Spring Security is available on the classpath.
     */
    private static final String SPRING_SECURITY_PATH = "org.springframework.security.core.context.SecurityContext";

    /**
     * Application context used to retrieve and create beans.
     */
    private ApplicationContext applicationContext;

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
     * Manages the transactions
     */
    private PlatformTransactionManager transactionManager;

    /**
     * Validator.
     */
    private Validator validator = new NoOpValidator();

    /**
     * Rest properties.
     */
    private RestProperties properties = new RestProperties();

    /**
     * Default handler mapping name.
     */
    private String defaultHandlerMappingName;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final RestHandlerMapping getObject() {
        afterPropertiesSet();

        RestHandlerMapping handlerMapping = new RestHandlerMapping(applicationContext, defaultHandlerMappingName);
        ResourceHandlerMappingFactory handlerMappingFactory = buildHandlerMappingFactory();
        for (Class<?> resourceClass : getAllResourceClasses()) {
            ResourceHandlerMapping resourceHandlerMapping = handlerMappingFactory.build(resourceClass);
            handlerMapping.registerCustomHandler(resourceHandlerMapping);
        }

        return handlerMapping;
    }
    
    private Set<Class<?>> getAllResourceClasses() {
        Assert.notNull(basePackage, "Base package is required.");
        
        Set<Class<?>> entityClasses = new HashSet<Class<?>>();
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(RestResource.class));
        Set<BeanDefinition> components = provider.findCandidateComponents(basePackage);
        for (BeanDefinition component : components) {
            entityClasses.add(Classes.forName(component.getBeanClassName()));
        }
        return entityClasses;
    }

    /**
     * Create a new factory, responsible for creating entity handler mappings.
     * 
     * @return the created factory
     */
    protected ResourceHandlerMappingFactory buildHandlerMappingFactory() {
        ResourceHandlerMappingFactory factory = new DefaultHandlerMappingFactory(objectMapper, conversionService, beanMapper, securityProvider, transactionManager, validator, properties);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(factory);
        return factory;
    }

    /**
     * Lazy initialization of underlying beans.
     */
    protected void afterPropertiesSet() {
        if (beanMapper == null) {
            beanMapper = buildBeanMapper();
        }
        if (securityProvider == null) {
            securityProvider = buildSecurityProvider();
        }
    }
    
    private BeanMapper buildBeanMapper() {
        return new BeanMapperBuilder()
                .addPackagePrefix(basePackage)
                .build();
    }
    
    private SecurityProvider buildSecurityProvider() {
        if (ClassUtils.isPresent(SPRING_SECURITY_PATH, getClass().getClassLoader())) {
            SecurityProvider securityProvider = new SpelSecurityProvider();
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
     * Sets the default handler mapping name.
     * @param defaultHandlerMappingName the new default handler mapping name
     */
    public void setDefaultHandlerMappingName(String defaultHandlerMappingName) {
        this.defaultHandlerMappingName = defaultHandlerMappingName;
    }
    
    /**
     * <i>Optionally</i> configure the base package.
     * @param basePackage the basePackage to set
     */
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
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
     * Set a transaction manager on our mappings.
     * @param transactionManager the transaction manager
     */
    @Autowired
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
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
     * Set the properties for handling REST requests.
     * @param properties the properties to set
     */
    @Autowired(required = false)
    public void setProperties(RestProperties properties) {
        this.properties = properties;
    }

}

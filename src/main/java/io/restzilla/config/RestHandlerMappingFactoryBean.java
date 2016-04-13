/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.config;

import static org.apache.commons.lang3.StringUtils.isBlank;
import io.beanmapper.BeanMapper;
import io.beanmapper.config.BeanMapperBuilder;
import io.beanmapper.utils.Classes;
import io.restzilla.RestInformation;
import io.restzilla.RestResource;
import io.restzilla.handler.DefaultHandlerMappingFactory;
import io.restzilla.handler.EntityHandlerMappingFactory;
import io.restzilla.handler.RestHandlerMapping;
import io.restzilla.handler.naming.CaseFormatNamingStrategy;
import io.restzilla.handler.naming.RestNamingStrategy;
import io.restzilla.handler.security.AlwaysSecurityProvider;
import io.restzilla.handler.security.SecurityProvider;
import io.restzilla.service.CrudServiceRegistry;
import io.restzilla.util.NoOpValidator;

import java.util.HashSet;
import java.util.Set;

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
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.HandlerMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;

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
     * Registry used to retrieve the underlying services.
     */
    private final CrudServiceRegistry crudServiceRegistry;
    
    /**
     * Application context used to retrieve and create beans.
     */
    private ApplicationContext applicationContext;

    /**
     * Generates the base paths per entity.
     */
    private RestNamingStrategy namingStrategy = new CaseFormatNamingStrategy(CaseFormat.LOWER_HYPHEN);

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
     * Create a new REST handler mapping factory bean.
     * @param serviceRegistry the service registry
     */
    public RestHandlerMappingFactoryBean(CrudServiceRegistry serviceRegistry) {
        this.crudServiceRegistry = Preconditions.checkNotNull(serviceRegistry, "Service registry is required.");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final RestHandlerMapping getObject() throws Exception {
        afterPropertiesSet();

        RestHandlerMapping restHandlerMapping = new RestHandlerMapping(applicationContext);
        EntityHandlerMappingFactory handlerMappingFactory = buildHandlerMappingFactory(crudServiceRegistry);
        for (Class entityClass : getEntityClasses()) {
            RestInformation entityInfo = buildInformation(entityClass);
            restHandlerMapping.registerHandler(handlerMappingFactory.build(entityInfo));
        }
        return restHandlerMapping;
    }
    
    private Set<Class<?>> getEntityClasses() {
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
     * @param serviceRegistry the service registry
     * @return the created factory
     */
    protected EntityHandlerMappingFactory buildHandlerMappingFactory(CrudServiceRegistry serviceRegistry) {
        DefaultHandlerMappingFactory factory = new DefaultHandlerMappingFactory(objectMapper, conversionService, beanMapper, securityProvider, validator);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(factory);
        return factory;
    }
    
    /**
     * Retrieve the information of an entity.
     * 
     * @param entityClass the entity class
     * @return the entity REST meta data
     * @throws NoSuchMethodException
     */
    private RestInformation buildInformation(Class<?> entityClass) throws NoSuchMethodException {
        RestResource annotation = entityClass.getAnnotationsByType(RestResource.class)[0];
        if (!annotation.value().equals(Object.class)) {
            entityClass = annotation.value();
        }
        String basePath = annotation.basePath();
        if (isBlank(basePath)) {
            basePath = namingStrategy.getBasePath(entityClass);
        }
        return new RestInformation(entityClass, basePath, annotation);
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
     * <i>Optionally</i> configure the base package.
     * @param basePackage the basePackage to set
     */
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
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

}

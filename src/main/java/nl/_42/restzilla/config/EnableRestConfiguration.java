/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.config;

import nl._42.restzilla.RestProperties;
import nl._42.restzilla.registry.CrudServiceRegistry;
import nl._42.restzilla.registry.RegistryConfiguration;
import nl._42.restzilla.service.ReadService;
import nl._42.restzilla.web.RestHandlerMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.web.PageableHandlerMethodArgumentResolverSupport;

import java.util.Map;

/**
 * Configuration imported when using @EnableRest.
 *
 * @author Jeroen van Schagen
 * @since Nov 12, 2015
 */
@Configuration
@Import(RegistryConfiguration.class)
public class EnableRestConfiguration implements ImportAware {
    
    private static final String BASE_PACKAGE_CLASS_NAME = "basePackageClass";
    private static final String DEFAULT_HANDLER_MAPPING_NAME = "defaultHandlerMappingName";

    private String basePackage;
    private String defaultHandlerMappingName;

    @Autowired(required = false)
    private PageableHandlerMethodArgumentResolverSupport pageableResolver;

    /**
     * Build a handler mapping, capable of delegating HTTP requests and fallback
     * handlers for the registered entities.
     *
     * @param applicationContext the application context
     * @return the handler mapping
     */
    @Bean
    public RestHandlerMapping restHandlerMapping(ApplicationContext applicationContext) {
        RestHandlerMappingFactoryBean factoryBean = new RestHandlerMappingFactoryBean();
        factoryBean.setBasePackage(basePackage);
        factoryBean.setDefaultHandlerMappingName(defaultHandlerMappingName);
        factoryBean.setApplicationContext(applicationContext);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(factoryBean);
        return factoryBean.getObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        Map<String, Object> attributes = importMetadata.getAnnotationAttributes(EnableRest.class.getName());
        basePackage = ((Class<?>) attributes.get(BASE_PACKAGE_CLASS_NAME)).getPackage().getName();
        defaultHandlerMappingName = (String) attributes.get(DEFAULT_HANDLER_MAPPING_NAME);
    }

    /**
     * Build a new service, capable of querying each type of registered entity.
     *
     * @param registry the CRUD registry
     * @return the read service
     */
    @Bean
    public ReadService readService(CrudServiceRegistry registry) {
        return new ReadService(registry);
    }

    /**
     * Read the REST properties.
     *
     * @param environment the environment
     * @return the properties
     */
    @Bean
    public RestProperties restProperties(Environment environment) {
        return new RestProperties(environment);
    }

}

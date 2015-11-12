/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.config;

import io.restzilla.handler.CrudHandlerMapping;
import io.restzilla.util.ClasspathChecker;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Configuration imported when using @EnableRest.
 *
 * @author Jeroen van Schagen
 * @since Nov 12, 2015
 */
@Configuration
public class EnableRestConfiguration implements ImportAware, InitializingBean, ApplicationContextAware {
    
    private static final String SWAGGER_PACKAGE = "com.mangofactory.swagger";

    private static final String BASE_PACKAGE_CLASS_NAME = "basePackageClass";

    private ApplicationContext applicationContext;

    private Map<String, Object> attributes;

    /**
     * Build a new CRUD handler mapping factory.
     * 
     * @return the mapping factory
     * @throws Exception whenever something goes wrong
     */
    @Bean
    public CrudHandlerMapping crudHandlerMapping() throws Exception {
        CrudHandlerMappingFactoryBean factoryBean = new CrudHandlerMappingFactoryBean();
        factoryBean.setBasePackageClass((Class<?>) attributes.get(BASE_PACKAGE_CLASS_NAME));
        factoryBean.setApplicationContext(applicationContext);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        attributes = importMetadata.getAnnotationAttributes(EnableRest.class.getName());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (ClasspathChecker.isOnClasspath(SWAGGER_PACKAGE)) {
            // Automatically register our swagger plugin when none are configured
            if (applicationContext.getBeansOfType(com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin.class).isEmpty()) {
                registerSwaggerPlugin();
            }
        }
    }

    private void registerSwaggerPlugin() throws Exception {
        com.mangofactory.swagger.configuration.SpringSwaggerConfig springSwaggerConfig = applicationContext.getBean(com.mangofactory.swagger.configuration.SpringSwaggerConfig.class);
        com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin swaggerSpringMvcPlugin = new io.restzilla.handler.swagger.SwaggerRestPlugin(springSwaggerConfig, crudHandlerMapping());
        ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        beanFactory.registerSingleton("swaggerSpringMvcPlugin", swaggerSpringMvcPlugin);
    }

}

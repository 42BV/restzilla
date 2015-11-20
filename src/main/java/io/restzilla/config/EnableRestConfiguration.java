/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.config;

import io.restzilla.handler.RestHandlerMapping;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
public class EnableRestConfiguration implements ImportAware, ApplicationContextAware {
    
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
    public RestHandlerMapping restHandlerMapping() throws Exception {
        RestHandlerMappingFactoryBean factoryBean = new RestHandlerMappingFactoryBean();
        factoryBean.setBasePackageClass((Class<?>) attributes.get(BASE_PACKAGE_CLASS_NAME));
        factoryBean.setApplicationContext(applicationContext);
        factoryBean.afterPropertiesSet();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(factoryBean);
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

}

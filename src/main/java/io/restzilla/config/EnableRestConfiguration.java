/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.config;

import io.restzilla.registry.AutoGenerateMapCrudServiceRegistry;
import io.restzilla.registry.DefaultServiceFactory;
import io.restzilla.registry.LazyRetrievalFactory;
import io.restzilla.service.CrudService;
import io.restzilla.registry.CrudServiceFactory;
import io.restzilla.registry.CrudServiceRegistry;
import io.restzilla.service.ReadService;
import io.restzilla.web.RestHandlerMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Map;

/**
 * Configuration imported when using @EnableRest.
 *
 * @author Jeroen van Schagen
 * @since Nov 12, 2015
 */
@Configuration
public class EnableRestConfiguration implements ImportAware, ApplicationContextAware {
    
    private static final String BASE_PACKAGE_CLASS_NAME = "basePackageClass";
    private static final String DEFAULT_HANDLER_MAPPING_NAME = "defaultHandlerMappingName";

    private ApplicationContext applicationContext;
    
    private CrudServiceFactory crudServiceFactory;

    private String basePackage;
    private String defaultHandlerMappingName;

    /**
     * Build a registry with references to each entity service, repository.
     * 
     * @return the service registry
     */
    @Bean
    public CrudServiceRegistry crudServiceRegistry() {
        if (crudServiceFactory == null) {
            crudServiceFactory = new DefaultServiceFactory(applicationContext);
        }

        LazyRetrievalFactory lazyFactory = new LazyRetrievalFactory(applicationContext, crudServiceFactory);
        AutoGenerateMapCrudServiceRegistry registry = new AutoGenerateMapCrudServiceRegistry(lazyFactory);

        // Auto register services and repositories

        for (CrudService crudService : applicationContext.getBeansOfType(CrudService.class).values()) {
            registry.registerService(crudService.getEntityClass(), crudService);
        }

        for (PagingAndSortingRepository crudRepository : applicationContext.getBeansOfType(PagingAndSortingRepository.class).values()) {
            Class<?>[] arguments = GenericTypeResolver.resolveTypeArguments(crudRepository.getClass(), CrudRepository.class);
            if (arguments != null && arguments.length == 2) {
                registry.registerRepository(arguments[0], crudRepository);
            }
        }

        return registry;
    }

    /**
     * Build a new service, capable of querying each type of registered entity.
     * 
     * @return the read service
     */
    @Bean
    public ReadService readService() {
        return new ReadService(crudServiceRegistry());
    }

    /**
     * Build a handler mapping, capable of delegating HTTP requests and fallback
     * handlers for the registered entities.
     * 
     * @return the handler mapping
     */
    @Bean
    public RestHandlerMapping restHandlerMapping() {
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
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Autowired(required = false)
    public void setCrudServiceFactory(CrudServiceFactory crudServiceFactory) {
        this.crudServiceFactory = crudServiceFactory;
    }

}

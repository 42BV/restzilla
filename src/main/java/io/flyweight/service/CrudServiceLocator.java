/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.flyweight.service;

import io.flyweight.RestEnable;
import io.flyweight.service.factory.CrudServiceFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.util.Assert;

/**
 * Factory bean that registers all services per entity.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class CrudServiceLocator {
    
    private final ApplicationContext applicationContext;

    public CrudServiceLocator(ApplicationContext applicationContext) {
        Assert.notNull(applicationContext, "Application context is required.");
        this.applicationContext = applicationContext;
    }

    /**
     * Register all services for the entities in the provided base package.
     * Whenever a service or repository is missing we will dynamically generate
     * an implementation bean, using the provided factory.
     * 
     * @param basePackage the base package to search for entities
     * @param factory the factory that creates missing repositories or reservices
     * @throws Exception whenever something goes wrong
     */
    public void registerAll(String basePackage, CrudServiceFactory factory) throws Exception {
        Assert.notNull(basePackage, "Base package is required.");

        Repositories repositories = new Repositories(applicationContext);
        Services services = new Services(applicationContext);

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(RestEnable.class));
        Set<BeanDefinition> components = provider.findCandidateComponents(basePackage);
        for (BeanDefinition component : components) {
            Class<?> entityClass = Class.forName(component.getBeanClassName());
            registerBeansFor(entityClass, services, repositories, factory);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void registerBeansFor(Class entityClass, Services services, Repositories repositories, CrudServiceFactory factory) throws Exception {
        PagingAndSortingRepository repository = getRepository(entityClass, repositories, factory);
        CrudService service = services.getByEntityClass(entityClass);
        if (service == null) {
            service = factory.buildService(entityClass, repository);
        }
        
        CrudServiceRegistry.register(entityClass, repository);
        CrudServiceRegistry.register(entityClass, service);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private PagingAndSortingRepository getRepository(Class entityClass, Repositories repositories, CrudServiceFactory factory) {
        Object repository = repositories.getRepositoryFor(entityClass);
        if (!(repository instanceof PagingAndSortingRepository)) {
            repository = factory.buildRepository(entityClass);
        }
        return (PagingAndSortingRepository) repository;
    }

    /**
     * Registry of all currently defined services. Only used for internal purposes.
     */
    private static class Services {
        
        private Map<Class<?>, CrudService<?, ?>> instances = new HashMap<Class<?>, CrudService<?, ?>>();
        
        @SuppressWarnings("rawtypes")
        public Services(ApplicationContext applicationContext) {
            Map<String, CrudService> services = applicationContext.getBeansOfType(CrudService.class);
            for (CrudService<?, ?> service : services.values()) {
                instances.put(service.getEntityClass(), service);
            }
        }
        
        public CrudService<?, ?> getByEntityClass(Class<?> entityClass) {
            return instances.get(entityClass);
        }

    }

}

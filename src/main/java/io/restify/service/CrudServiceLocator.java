/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.service;

import io.restify.RestEnable;
import io.restify.repository.SpringDataJpaRepositoryFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.support.Repositories;

/**
 * Factory bean that creates a registry from all entities to their services.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class CrudServiceLocator {
    
    private final CrudServiceRegistry registry = CrudServiceRegistry.getInstance();

    private final ApplicationContext applicationContext;

    private final String basePackage;

    public CrudServiceLocator(ApplicationContext applicationContext, String basePackage) {
        if (basePackage == null) {
            throw new IllegalStateException("Base package is required.");
        }
        this.applicationContext = applicationContext;
        this.basePackage = basePackage;
    }

    /**
     * Locate the CrudServices for all entities annotated with @EnableRest.
     * When no repository or service yet exists, we will dynamically create
     * a new instance. Instantiation behaviour can be overwritten by subclasses.
     * 
     * @return registry with all service beans per entity class
     * @throws Exception whenever something goes wrong
     */
    public CrudServiceRegistry execute() throws Exception {
        Repositories repositories = new Repositories(applicationContext);
        Services services = new Services(applicationContext);

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(RestEnable.class));
        Set<BeanDefinition> components = provider.findCandidateComponents(basePackage);
        for (BeanDefinition component : components) {
            Class<?> entityClass = Class.forName(component.getBeanClassName());
            registerBeansForEntity(entityClass, services, repositories);
        }
        
        return registry;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void registerBeansForEntity(Class entityClass, Services services, Repositories repositories) throws Exception {
        PagingAndSortingRepository repository = getRepository(entityClass, repositories);
        registry.register(entityClass, repository);

        CrudService service = services.getByEntityClass(entityClass);
        if (service == null) {
            service = buildNewService(entityClass, repository);
        }
        registry.register(entityClass, service);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private PagingAndSortingRepository getRepository(Class entityClass, Repositories repositories) {
        Object repository = repositories.getRepositoryFor(entityClass);
        if (!(repository instanceof PagingAndSortingRepository)) {
            repository = buildNewRepository(entityClass);
        }
        return (PagingAndSortingRepository) repository;
    }

    /**
     * Build a new CrudRepository for the entity.
     * 
     * @param entityClass the entity class
     * @param beanFactory the bean factory, used for injecting dependencies
     * @return the repository bean
     */
    protected <T extends Persistable<ID>, ID extends Serializable> CrudRepository<T, ID> buildNewRepository(Class<T> entityClass) {
        return new SpringDataJpaRepositoryFactory<T, ID>(applicationContext.getAutowireCapableBeanFactory(), entityClass).build();
    }
    
    /**
     * Build a new CrudService for the entity.
     * @param entityClass the entity class
     * @param repository the delegate repository
     * @param beanFactory the bean factory, used for injecting dependencies
     * @return the service bean
     */
    protected <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> buildNewService(Class<T> entityClass, PagingAndSortingRepository<T, ID> repository) {
        TransactionalCrudService<T, ID> service = new TransactionalCrudService<T, ID>(repository, entityClass);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(service);
        return service;
    }

    /**
     * Retrieve the application context.
     * @return the application context
     */
    protected final ApplicationContext getApplicationContext() {
        return applicationContext;
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

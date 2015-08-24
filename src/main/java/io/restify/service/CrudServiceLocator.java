/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.service;

import io.restify.EnableRest;
import io.restify.repository.SpringDataJpaRepositoryFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.support.Repositories;

/**
 * Factory bean that creates a registry from all entities to their services.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class CrudServiceLocator {
    
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
        CrudServiceRegistry result = new CrudServiceRegistry();

        Repositories repositories = new Repositories(applicationContext);
        Services services = new Services(applicationContext);

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(EnableRest.class));
        Set<BeanDefinition> components = provider.findCandidateComponents(basePackage);
        for (BeanDefinition component : components) {
            Class<?> entityClass = Class.forName(component.getBeanClassName());
            CrudService<?, ?> service = getService(entityClass, services, repositories);
            result.register(entityClass, service);
        }
        
        return result;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private CrudService<?, ?> getService(Class<?> entityClass, Services services, Repositories repositories) throws Exception {
        CrudService service = services.getByEntityClass(entityClass);
        if (service == null) {
            AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
            Object repository = repositories.getRepositoryFor(entityClass);
            if (!(repository instanceof CrudRepository)) {
                repository = buildNewRepository(entityClass, beanFactory);
            }
            service = buildNewService(entityClass, (CrudRepository) repository, beanFactory);
        }
        return service;
    }

    /**
     * Build a new CrudRepository for the entity.
     * 
     * @param entityClass the entity class
     * @param beanFactory the bean factory, used for injecting dependencies
     * @return the repository bean
     */
    protected <T, ID extends Serializable> CrudRepository<T, ID> buildNewRepository(Class<T> entityClass, AutowireCapableBeanFactory beanFactory) {
        return new SpringDataJpaRepositoryFactory<T, ID>(beanFactory, entityClass).build();
    }
    
    /**
     * Build a new CrudService for the entity.
     * @param entityClass the entity class
     * @param repository the delegate repository
     * @param beanFactory the bean factory, used for injecting dependencies
     * @return the service bean
     */
    protected <T, ID extends Serializable> CrudService<T, ID> buildNewService(Class<T> entityClass, CrudRepository<T, ID> repository, AutowireCapableBeanFactory beanFactory) {
        CrudService<T, ID> service = new TransactionalCrudService<T, ID>(repository, entityClass);
        beanFactory.autowireBean(service);
        return service;
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

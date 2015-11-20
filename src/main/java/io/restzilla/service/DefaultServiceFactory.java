/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service;

import io.restzilla.service.impl.DefaultCrudService;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the service factory.
 *
 * @author Jeroen van Schagen
 * @since Sep 18, 2015
 */
public class DefaultServiceFactory implements CrudServiceFactory {
    
    private final ConfigurableListableBeanFactory beanFactory;
    
    @PersistenceContext
    private EntityManager entityManager;

    public DefaultServiceFactory(ApplicationContext applicationContext) {
        beanFactory = (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();        
        beanFactory.autowireBean(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Persistable<ID>, ID extends Serializable> PagingAndSortingRepository<T, ID> buildRepository(Class<T> entityClass) {
        SimpleJpaRepository<T, ID> repository = new SimpleJpaRepository<T, ID>(entityClass, getEntityManager());
        beanFactory.autowireBean(repository);
        return repository;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> buildService(Class<T> entityClass, PagingAndSortingRepository<T, ID> repository) {
        DefaultCrudService<T, ID> service = new DefaultCrudService<T, ID>(entityClass, repository);
        beanFactory.autowireBean(service);

        final String beanName = StringUtils.uncapitalize(entityClass.getSimpleName()) + "Service";
        Object proxy = beanFactory.applyBeanPostProcessorsAfterInitialization(service, beanName);
        beanFactory.registerSingleton(beanName, proxy);
        return (CrudService<T, ID>) proxy;
    }
    
    /**
     * Retrieve the entity manager.
     * 
     * @return the entity manager
     */
    public final EntityManager getEntityManager() {
        return entityManager;
    }

}

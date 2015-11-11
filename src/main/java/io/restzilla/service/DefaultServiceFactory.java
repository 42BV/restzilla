/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service;

import io.restzilla.service.impl.TransactionalCrudService;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Default implementation of the service factory.
 *
 * @author Jeroen van Schagen
 * @since Sep 18, 2015
 */
public class DefaultServiceFactory implements CrudServiceFactory {
    
    private final AutowireCapableBeanFactory beanFactory;

    @PersistenceContext
    private EntityManager entityManager;

    public DefaultServiceFactory(ApplicationContext applicationContext) {
        beanFactory = applicationContext.getAutowireCapableBeanFactory();
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
    public <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> buildService(Class<T> entityClass, PagingAndSortingRepository<T, ID> repository) {
        TransactionalCrudService<T, ID> service = new TransactionalCrudService<T, ID>(repository, entityClass);
        beanFactory.autowireBean(service);
        return service;
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

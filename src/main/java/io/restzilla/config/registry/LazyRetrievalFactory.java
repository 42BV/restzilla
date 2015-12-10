/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.config.registry;

import io.restzilla.service.CrudService;
import io.restzilla.service.CrudServiceFactory;

import java.io.Serializable;

import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.support.Repositories;

/**
 * Service factory that lazy retrieves from the application context.
 * By lazy retrieval you can be certain that all beans are registered before retrieval.
 *
 * @author Jeroen van Schagen
 * @since Dec 10, 2015
 */
@SuppressWarnings("unchecked")
public class LazyRetrievalFactory implements CrudServiceFactory {
    
    private final Repositories repositories;
    
    private final Services services;
    
    private final CrudServiceFactory delegate;

    public LazyRetrievalFactory(ApplicationContext applicationContext, CrudServiceFactory delegate) {
        this.repositories = new Repositories(applicationContext);
        this.services = new Services(applicationContext);
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Persistable<ID>, ID extends Serializable> PagingAndSortingRepository<T, ID> buildRepository(Class<T> entityClass) {
        Object repository = repositories.getRepositoryFor(entityClass);
        if (repository instanceof PagingAndSortingRepository) {
            return (PagingAndSortingRepository<T, ID>) repository;
        } else {
            return delegate.buildRepository(entityClass);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> buildService(Class<T> entityClass, PagingAndSortingRepository<T, ID> repository) {
        CrudService<?, ?> service = services.getByEntityClass(entityClass);
        if (service != null) {
            return (CrudService<T, ID>) service;
        } else {
            return delegate.buildService(entityClass, repository);
        }
    }

}

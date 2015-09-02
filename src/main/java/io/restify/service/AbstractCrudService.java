/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Template implementation of the CrudService, delegates all to the repository.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public abstract class AbstractCrudService<T, ID extends Serializable> implements CrudService<T, ID> {

    private final PagingAndSortingRepository<T, ID> repository;
    
    private final Class<T> entityClass;

    public AbstractCrudService(Class<T> entityClass, PagingAndSortingRepository<T, ID> repository) {
        this.repository = repository;
        this.entityClass = entityClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> findAll() {
        return (List<T>) repository.findAll();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public T findOne(ID id) {
        return repository.findOne(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends T> S save(S entity) {
        return repository.save(entity);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(ID id) {
        repository.delete(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

}

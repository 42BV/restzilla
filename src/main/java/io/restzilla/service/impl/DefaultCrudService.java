/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service.impl;

import io.restzilla.service.CrudService;
import io.restzilla.service.RepositoryAware;

import java.io.Serializable;
import java.util.List;

import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.util.Assert;

/**
 * Default implementation of the CRUD service, delegates all to the repository.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class DefaultCrudService<T extends Persistable<ID>, ID extends Serializable> extends CrudServiceSupport<T, ID> implements RepositoryAware<T, ID> {

    private final Class<T> entityClass;

    private PagingAndSortingRepository<T, ID> repository;

    /**
     * Construct a new service.
     * <br>
     * <b>This constructor dynamically resolves the entity type with reflection.</b>
     */
    @SuppressWarnings("unchecked")
    public DefaultCrudService() {
        this.entityClass = (Class<T>) resolveEntityType();
        Assert.notNull(entityClass, "Entity class cannot be null");
    }

    private Class<?> resolveEntityType() {
        return GenericTypeResolver.resolveTypeArguments(getClass(), CrudService.class)[0];
    }
    
    /**
     * Construct a new service.
     * 
     * @param entityClass the entity class
     */
    public DefaultCrudService(Class<T> entityClass) {
        Assert.notNull(entityClass, "Entity class cannot be null");
        this.entityClass = entityClass;
    }

    /**
     * Construct a new service.
     * <br>
     * <b>This constructor dynamically resolves the entity type with reflection.</b>
     * 
     * @param repository the repository
     */
    public DefaultCrudService(PagingAndSortingRepository<T, ID> repository) {
        this(); // Dynamically resolve entity class
        Assert.notNull(repository, "Repository cannot be null");
        this.repository = repository;
    }

    /**
     * Construct a new service.
     * 
     * @param entityClass the entity class
     * @param repository the repository
     */
    public DefaultCrudService(Class<T> entityClass, PagingAndSortingRepository<T, ID> repository) {
        this(entityClass);
        Assert.notNull(repository, "Repository cannot be null");
        this.repository = repository;
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
    public List<T> findAll(Sort sort) {
        return (List<T>) repository.findAll(sort);
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PagingAndSortingRepository<T, ID> getRepository() {
        return repository;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setRepository(PagingAndSortingRepository<T, ID> repository) {
        this.repository = repository;
    }

}

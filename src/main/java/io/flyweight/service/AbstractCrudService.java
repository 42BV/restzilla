/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.flyweight.service;

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
 * Template implementation of the CrudService, delegates all to the repository.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public abstract class AbstractCrudService<T extends Persistable<ID>, ID extends Serializable> implements CrudService<T, ID> {

    private final Class<T> entityClass;

    private PagingAndSortingRepository<T, ID> repository;

    /**
     * Construct a new service.
     * <br>
     * <b>This constructor dynamically resolves the entity type with reflection.</b>
     */
    @SuppressWarnings("unchecked")
    public AbstractCrudService() {
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
    @SuppressWarnings("unchecked")
    public AbstractCrudService(Class<T> entityClass) {
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
    @SuppressWarnings("unchecked")
    public AbstractCrudService(PagingAndSortingRepository<T, ID> repository) {
        this(); // Dynamically resolve entity type
        Assert.notNull(repository, "Repository cannot be null");
        this.repository = repository;
    }

    /**
     * Construct a new service.
     * 
     * @param entityClass the entity class
     * @param repository the repository
     */
    public AbstractCrudService(Class<T> entityClass, PagingAndSortingRepository<T, ID> repository) {
        this(entityClass); // Store entity type
        Assert.notNull(repository, "Repository cannot be null");
        this.repository = repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> findAll() {
        return (List<T>) getRepository().findAll();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> findAll(Sort sort) {
        return (List<T>) getRepository().findAll(sort);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Page<T> findAll(Pageable pageable) {
        return getRepository().findAll(pageable);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public T findOne(ID id) {
        return getRepository().findOne(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends T> S save(S entity) {
        return getRepository().save(entity);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(ID id) {
        getRepository().delete(id);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(T entity) {
        delete(entity.getId());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }
    
    /**
     * Retrieve the underlying repository.
     * 
     * @return the repository
     */
    @SuppressWarnings("unchecked")
    protected PagingAndSortingRepository<T, ID> getRepository() {
        if (repository == null) {
            // Whenever no repository is provided, dynamically retrieve it from our registry
            repository = (PagingAndSortingRepository<T, ID>) CrudServiceRegistry.getRepository(entityClass);
        }
        return repository;
    }

}

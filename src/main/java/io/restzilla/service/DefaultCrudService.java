/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service;

import io.beanmapper.spring.Lazy;

import java.io.Serializable;
import java.util.List;
import java.util.function.Supplier;

import javax.persistence.EntityNotFoundException;

import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Default implementation of the CRUD service, delegates all to the repository.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class DefaultCrudService<T extends Persistable<ID>, ID extends Serializable> implements CrudService<T, ID>, RepositoryAware<T, ID> {

    private static Cache EMPTY_CACHE = new NoOpCacheManager().getCache("empty");

    /**
     * Class reference to the type of entities that we manage
     * in this service instance.
     */
    private final Class<T> entityClass;

    /**
     * Repository used to communicate with the database. Note that
     * this instance is not marked as final, because it can be
     * dynamically injected at runtime.
     */
    private PagingAndSortingRepository<T, ID> repository;

    private Cache cache = EMPTY_CACHE;

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

    /**
     * Dynamically resolve the entity type based on generic type arguments.
     * 
     * @return the resolved entity type
     */
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
    @Transactional(readOnly = true)
    public List<T> findAll() {
        return getByCacheOrExecute("findAll()", () -> (List<T>) repository.findAll());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<T> findAll(Sort sort) {
        return (List<T>) repository.findAll(sort);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public T findOne(ID id) {
        if (id == null) {
            return null;
        }
        
        final String key = "findOne(" + id + ")";
        return getByCacheOrExecute(key, () -> repository.findOne(id));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public T getOne(ID id) {
        T entity = findOne(id);
        if (entity == null) {
            throw new EntityNotFoundException("Could not find entity '" + getEntityClass().getSimpleName() + "' with id: " + id);
        }
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public <S extends T> S save(S entity) {
        S result = repository.save(entity);
        cache.clear();
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public <S extends T> S save(Lazy<S> entity) {
        return save(entity.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(ID id) {
        repository.delete(id);
        cache.clear();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(T entity) {
        if (!entity.isNew()) {
            delete(entity.getId());
        }
    }

    @SuppressWarnings("unchecked")
    private <R> R getByCacheOrExecute(String key, Supplier<R> retriever) {
        ValueWrapper cached = cache.get(key);
        if (cached == null) {
            R result = retriever.get();
            cache.put(key, result);
            return result;
        } else {
            return (R) cached.get();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }
    
    public PagingAndSortingRepository<T, ID> getRepository() {
        return repository;
    }
    
    public void setRepository(PagingAndSortingRepository<T, ID> repository) {
        this.repository = repository;
    }
    
    public Cache getCache() {
        return cache;
    }
    
    public void setCache(Cache cache) {
        this.cache = cache;
    }

}

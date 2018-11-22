/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.service;

import com.google.common.collect.Lists;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Default implementation of the CRUD service, delegates all to the repository.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class DefaultCrudService<T extends Persistable<ID>, ID extends Serializable> extends AbstractCrudService<T, ID> {

    private static Cache EMPTY_CACHE = new NoOpCacheManager().getCache("empty");

    /**
     * Cache used internally for storing entities.
     */
    private Cache cache = EMPTY_CACHE;

    /**
     * Repository used to communicate with the database. Note that
     * this instance is not marked as final, because it can be
     * dynamically injected at runtime.
     */
    private PagingAndSortingRepository<T, ID> repository;


    /**
     * Construct a new service.
     * <br>
     * <b>This constructor dynamically resolves the entity type with reflection.</b>
     */
    @SuppressWarnings("unchecked")
    public DefaultCrudService() {
        super(); // Dynamically resolve entity class
    }
    
    /**
     * Construct a new service.
     * 
     * @param entityClass the entity class
     */
    public DefaultCrudService(Class<T> entityClass) {
        super(entityClass);
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

    @Override
    public List<T> findAll(Iterable<ID> ids) {
        return Lists.newArrayList(repository.findAllById(ids));
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
        return getByCacheOrExecute(key, () -> repository.findById(id).orElse(null));
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
    public void delete(T entity) {
        if (entity != null) {
            repository.delete(entity);
            cache.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(ID id) {
        if (id != null) {
            repository.deleteById(id);
            cache.clear();
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
     * Retrieves the repository.
     * @return repository
     */
    public PagingAndSortingRepository<T, ID> getRepository() {
        return repository;
    }
    
    /**
     * Modifies the repository.
     * @param repository the new repository
     */
    public void setRepository(PagingAndSortingRepository<T, ID> repository) {
        this.repository = repository;
    }
    
    /**
     * Retrieves the cache.
     * @return cache
     */
    public Cache getCache() {
        return cache;
    }
    
    /**
     * Modifies the cache.
     * @param cache the new cache
     */
    public void setCache(Cache cache) {
        this.cache = cache;
    }

}

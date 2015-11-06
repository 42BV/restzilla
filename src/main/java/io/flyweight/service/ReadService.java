/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.flyweight.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Service capable of reading any type of entity.
 * <br/><br/>
 * <b>This service is strictly for internal usage and should
 * not be exposed to the outside.</b>
 *
 * @author Jeroen van Schagen
 * @since Nov 6, 2015
 */
public class ReadService {
    
    private final CrudServiceFactory factory;
    
    public ReadService(CrudServiceFactory factory) {
        this.factory = factory;
    }

    /**
     * Retrieve all entities with a certain sort.
     * 
     * @param entityClass the entity class
     * @param sort the sorting
     * @return the sorted entities
     */
    public <T> List<T> findAll(Class<T> entityClass, Sort sort) {
        PagingAndSortingRepository<T, ?> repository = getRepository(entityClass);
        return (List<T>) repository.findAll(sort);
    }

    /**
     * Retrieve a page of entities.
     * 
     * @param entityClass the entity class
     * @param pageable the pageable
     * @return the page of entities
     */
    public <T> Page<T> findAll(Class<T> entityClass, Pageable pageable) {
        PagingAndSortingRepository<T, ?> repository = getRepository(entityClass);
        return repository.findAll(pageable);
    }

    /**
     * Retrieve a specific entity, with an identifier.
     * 
     * @param entityClass the entity class
     * @param primaryKey the primary key
     * @return the result entity, if any
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T getOne(Class<T> entityClass, Serializable primaryKey) {
        PagingAndSortingRepository repository = getRepository(entityClass);
        T entity = (T) repository.findOne(primaryKey);
        if (entity == null) {
            throw new IllegalArgumentException("Could not find entity '" + entityClass.getSimpleName() + "' with id: " + primaryKey);
        }
        return entity;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <T> PagingAndSortingRepository<T, ?> getRepository(Class entityClass) {
        PagingAndSortingRepository repository = CrudServiceRegistry.getRepository(entityClass);
        if (repository == null) {
            repository = factory.buildRepository(entityClass);
            CrudServiceRegistry.register(entityClass, repository);
        }
        return repository;
    }

}

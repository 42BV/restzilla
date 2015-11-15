/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service;

import io.beanmapper.spring.Lazy;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Persistable;

/**
 * CRUD service.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public interface CrudService<T extends Persistable<ID>, ID extends Serializable> extends Listable<T> {
    
    /**
     * Returns all entities.
     * 
     * @return all entities
     */
    List<T> findAll();
    
    /**
     * Retrieves an entity by its id.
     * 
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal null} if none found
     * @throws IllegalArgumentException if {@code id} is {@literal null}
     */
    T findOne(ID id);
    
    /**
     * Retrieves an entity by its id, but when the value is null we throw an exception.
     * 
     * @param id must not be {@literal null}.
     * @return the entity with the given id
     * @throws IllegalArgumentException if {@code id} is {@literal null} or the result cannot be found
     */
    T getOne(ID id);
    
    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     * 
     * @param entity the entity
     * @return the saved entity
     */
    <S extends T> S save(S entity);
    
    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     * 
     * @param entity the lazy entity
     * @return the saved entity
     */
    <S extends T> S save(Lazy<S> entity);
    
    /**
     * Deletes the entity with the given id.
     * 
     * @param id must not be {@literal null}.
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}
     */
    void delete(ID id);
    
    /**
     * Deletes the entity.
     * 
     * @param entity must not be {@literal null}.
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}
     */
    void delete(T entity);
    
    /**
     * Retrieve the entity class.
     * 
     * @return the entity class
     */
    Class<T> getEntityClass();

}

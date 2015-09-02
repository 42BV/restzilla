/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * CRUD service.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public interface CrudService<T, ID extends Serializable> {
    
    /**
     * Returns all entities.
     * 
     * @return all entities
     */
    List<T> findAll();
    
    /**
     * Returns a page of entities.
     * 
     * @param pageable the pageable
     * @return the entities in that page
     */
    Page<T> findAll(Pageable pageable);
    
    /**
     * Retrieves an entity by its id.
     * 
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal null} if none found
     * @throws IllegalArgumentException if {@code id} is {@literal null}
     */
    T findOne(ID id);
    
    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     * 
     * @param entity
     * @return the saved entity
     */
    <S extends T> S save(S entity);
    
    /**
     * Deletes the entity with the given id.
     * 
     * @param id must not be {@literal null}.
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}
     */
    void delete(ID id);
    
    /**
     * Retrieve the entity class.
     * 
     * @return the entity class
     */
    Class<T> getEntityClass();

}

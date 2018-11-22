/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.service;

import io.beanmapper.spring.Lazy;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;

/**
 * CRUD service.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public interface CrudService<T extends Persistable<ID>, ID extends Serializable> {
    
    /**
     * Returns all entities.
     * 
     * @return all entities
     */
    List<T> findAll();

    /**
     * Returns all entities matching the specified IDs
     * @param ids IDs to find the entities for.
     * @return All found entities.
     */
    List<T> findAll(Iterable<ID> ids);
    
    /**
     * Returns all entities, sorted.
     * 
     * @param sort the sort
     * @return all entities
     */
    List<T> findAll(Sort sort);
    
    /**
     * Returns a page of entities.
     * 
     * @param pageable the pageable
     * @return the entities in that page
     */
    Page<T> findAll(Pageable pageable);
    
    /**
     * Retrieve an optional entity based on its identifier.
     * 
     * @param id the identifier
     * @return the optional entity
     */
    Optional<T> find(ID id);
    
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
     * @param <S> the result type
     * @param entity the entity
     * @return the saved entity
     */
    <S extends T> S save(S entity);
    
    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     * 
     * @param <S> the result type
     * @param entity the lazy entity
     * @return the saved entity
     */
    <S extends T> S save(Lazy<S> entity);

    /**
     * Deletes the entity.
     * 
     * @param entity must not be {@literal null}.
     */
    void delete(T entity);

    /**
     * Deletes the entity by identifier.
     *
     * @param id must not be {@literal null}.
     */
    void delete(ID id);

    /**
     * Retrieve the entity class.
     *
     * @return the entity class
     */
    Class<T> getEntityClass();

}

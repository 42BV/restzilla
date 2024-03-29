/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.service;

import org.springframework.data.domain.Persistable;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Objects;

/**
 * CRUD service.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public interface CrudService<T extends Persistable<ID>, ID extends Serializable> extends PagingAndSortingService<T, ID> {

    /**
     * Saves a given entity. Use the returned instance for further
     * operations as this save could return a wrapped object.
     * 
     * @param <S> the result type
     * @param entity the entity
     * @return the saved entity
     */
    <S extends T> S save(S entity);

    /**
     * Deletes the entity.
     *
     * @param entity the entity to delete
     */
    void delete(T entity);

}

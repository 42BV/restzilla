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
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD service.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public interface CrudService<T extends Persistable<ID>, ID extends Serializable> extends PagingAndSortingService<T, ID> {

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     * 
     * @param <S> the result type
     * @param entity the entity
     * @return the saved entity
     */
    @Transactional
    default <S extends T> S save(S entity) {
        return getRepository().save(entity);
    }
    
    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     * 
     * @param <S> the result type
     * @param entity the lazy entity
     * @return the saved entity
     */
    @Transactional
    default <S extends T> S save(Lazy<S> entity) {
        try {
            return save(entity.get());
        } catch (RuntimeException rte) {
            throw rte;
        } catch (Exception e) {
            throw new LazyEntityRetrievalException(e);
        }
    }

    /**
     * Deletes the entity.
     * 
     * @param entity must not be {@literal null}.
     */
    @Transactional
    default void delete(T entity) {
        getRepository().delete(entity);
    }

    /**
     * Deletes the entity by identifier.
     *
     * @param id must not be {@literal null}.
     */
    @Transactional
    default void delete(ID id) {
        getRepository().deleteById(id);
    }

    /**
     * Retrieve the entity class.
     *
     * @return the entity class
     */
    Class<T> getEntityClass();

}

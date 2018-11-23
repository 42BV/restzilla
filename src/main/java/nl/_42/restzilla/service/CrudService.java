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
    @Transactional
    default <S extends T> S save(S entity) {
        Objects.requireNonNull(entity, "Cannot save a null entity");
        return getRepository().save(entity);
    }
    
    /**
     * Saves a given entity. Use the returned instance for further
     * operations as this save could return a wrapped object.
     *
     * The entity is provided as a lazy value, allowing us to perform
     * modifications inside of the service transaction. Without a
     * transaction, it would be impossible to fetch dependencies in
     * the entity tree.
     * 
     * @param <S> the result type
     * @param supplier the entity supplier
     * @return the saved entity
     */
    @Transactional
    default <S extends T> S save(final Lazy<S> supplier) {
        return this.save(
          supplier.apply()
        );
    }

    /**
     * Deletes the entity by identifier.
     *
     * @param id must not be {@literal null}.
     */
    @Transactional
    default void delete(ID id) {
        Objects.requireNonNull(id, "Cannot delete based on a null identifier");
        getRepository().deleteById(id);
    }

    /**
     * Deletes the entity.
     *
     * @param entity the entity to delete
     */
    @Transactional
    default void delete(T entity) {
        Objects.requireNonNull(entity, "Cannot delete a null entity");
        getRepository().delete(entity);
    }

    /**
     * Deletes the entity.
     *
     * The entity is provided as a lazy value, allowing us to perform
     * modifications inside of the service transaction.
     *
     * @param supplier the entity supplier
     */
    @Transactional
    default void delete(final Lazy<T> supplier) {
        this.delete(
          supplier.apply()
        );
    }

}

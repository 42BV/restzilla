/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service.impl;

import io.restzilla.service.CrudService;
import io.restzilla.service.Lazy;

import java.io.Serializable;

import org.springframework.data.domain.Persistable;

/**
 * Provides template implementations for the CRUD service interface.
 *
 * @author Jeroen van Schagen
 * @since Nov 14, 2015
 */
public abstract class CrudServiceSupport<T extends Persistable<ID>, ID extends Serializable> implements CrudService<T, ID> {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public T getOne(ID id) {
        T entity = findOne(id);
        if (entity == null) {
            throw new IllegalArgumentException("Could not find entity '" + getEntityClass().getSimpleName() + "' with id: " + id);
        }
        return entity;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends T> S save(Lazy<S> entity) {
        return save(entity.get());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(T entity) {
        if (!entity.isNew()) {
            delete(entity.getId());
        }
    }
    
}

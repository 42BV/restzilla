/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service.wrapper;

import io.restzilla.service.Listable;
import io.restzilla.service.ReadService;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;

/**
 * Adapts the {@link ReadService} to the {@link Listable} interface.
 *
 * @author Jeroen van Schagen
 * @since Nov 6, 2015
 */
public class ReadServiceListable<T extends Persistable<ID>, ID extends Serializable> implements Listable<T> {
    
    private final ReadService readService;

    private final Class<T> entityClass;
    
    /**
     * Create a new {@link ReadServiceListable} instance.
     * 
     * @param readService the read service
     * @param entityClass the entity class
     */
    public ReadServiceListable(ReadService readService, Class<T> entityClass) {
        this.readService = readService;
        this.entityClass = entityClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> findAll(Sort sort) {
        return readService.findAll(entityClass, sort);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Page<T> findAll(Pageable pageable) {
        return readService.findAll(entityClass, pageable);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getEntityClass() {
        return entityClass;
    }

}

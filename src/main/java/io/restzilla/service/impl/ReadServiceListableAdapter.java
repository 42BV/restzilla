/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service.impl;

import io.restzilla.service.Listable;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;

/**
 * Create a read service wrapper that attaches us to a specific entity class.
 *
 * @author Jeroen van Schagen
 * @since Nov 6, 2015
 */
public class ReadServiceListableAdapter<T extends Persistable<ID>, ID extends Serializable> implements Listable<T> {
    
    private final ReadService readService;

    private final Class<T> entityClass;
    
    public ReadServiceListableAdapter(ReadService readService, Class<T> entityClass) {
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
    
}

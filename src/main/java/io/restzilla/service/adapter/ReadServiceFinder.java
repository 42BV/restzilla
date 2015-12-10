/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service.adapter;

import io.beanmapper.spring.web.EntityFinder;
import io.restzilla.service.ReadService;

/**
 * Adapts the {@link ReadService} to the {@link EntityFinder} interface.
 *
 * @author Jeroen van Schagen
 * @since Nov 24, 2015
 */
public class ReadServiceFinder implements EntityFinder {
    
    private final ReadService readService;

    /**
     * Create a new {@link ReadServiceFinder} instance.
     * 
     * @param readService the read service
     */
    public ReadServiceFinder(ReadService readService) {
        this.readService = readService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object find(Long id, Class entityClass) {
        return readService.getOne(entityClass, id);
    }
    
}

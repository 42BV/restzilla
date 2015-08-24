/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry of all entities that require REST and their services.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class CrudServiceRegistry {
    
    private Map<Class<?>, CrudService<?, ?>> instances = new HashMap<Class<?>, CrudService<?, ?>>();

    /**
     * Retrieve all entity classes.
     * @return the entity classes
     */
    public Set<Class<?>> getEntityClasses() {
        return instances.keySet();
    }

    /**
     * Retrieve the CRUD service for our entity.
     * @param entityClass the entity class
     * @return the CRUD service
     */
    public CrudService<?, ?> getService(Class<?> entityClass) {
        return instances.get(entityClass);
    }
    
    /**
     * Register a new service instance.
     * @param entityClass the entity class
     * @param instance the instance
     */
    public void register(Class<?> entityClass, CrudService<?, ?> instance) {
        instances.put(entityClass, instance);
    }

}
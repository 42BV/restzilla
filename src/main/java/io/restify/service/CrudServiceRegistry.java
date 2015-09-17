/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Registry of all entities that require REST and their services.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class CrudServiceRegistry {
    
    /**
     * Reference to the singleton instance of our registry.
     */
    private static CrudServiceRegistry INSTANCE;
    
    // Delegate instances

    private final Map<Class<?>, PagingAndSortingRepository<?, ?>> repositories;
    
    private final Map<Class<?>, CrudService<?, ?>> services;
    
    private CrudServiceRegistry() {
        repositories = new HashMap<Class<?>, PagingAndSortingRepository<?, ?>>();
        services = new HashMap<Class<?>, CrudService<?, ?>>();
    }
    
    /**
     * Retrieve the instance.
     * @return the instance
     */
    public static CrudServiceRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CrudServiceRegistry();
        }
        return INSTANCE;
    }

    /**
     * Retrieve all entity classes.
     * @return the entity classes
     */
    public Set<Class<?>> getEntityClasses() {
        return services.keySet();
    }

    /**
     * Retrieve the CRUD service for our entity.
     * @param entityClass the entity class
     * @return the CRUD service
     */
    public CrudService<?, ?> getService(Class<?> entityClass) {
        return services.get(entityClass);
    }
    
    /**
     * Retrieve the CRUD repository for our entity.
     * @param entityClass the entity class
     * @return the CRUD repository
     */
    PagingAndSortingRepository<?, ?> getRepository(Class<?> entityClass) {
        return repositories.get(entityClass);
    }
    
    /**
     * Register a new service instance.
     * @param entityClass the entity class
     * @param instance the instance
     */
    void register(Class<?> entityClass, CrudService<?, ?> instance) {
        services.put(entityClass, instance);
    }
    
    /**
     * Register a new repository instance.
     * @param entityClass the entity class
     * @param instance the instance
     */
    void register(Class<?> entityClass, PagingAndSortingRepository<?, ?> instance) {
        repositories.put(entityClass, instance);
    }

}
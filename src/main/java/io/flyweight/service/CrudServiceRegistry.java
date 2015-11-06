/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.flyweight.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Registers all service and repository instances per entity class.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class CrudServiceRegistry {

    private static final Map<Class<?>, PagingAndSortingRepository<?, ?>> REPOSITORIES;
    
    private static final Map<Class<?>, CrudService<?, ?>> SERVICES;
    
    static {
        REPOSITORIES = new HashMap<Class<?>, PagingAndSortingRepository<?, ?>>();
        SERVICES = new HashMap<Class<?>, CrudService<?, ?>>();
    }

    /**
     * Retrieve all entity classes.
     * 
     * @return the entity classes
     */
    public static Set<Class<?>> getEntityClasses() {
        return SERVICES.keySet();
    }

    /**
     * Retrieve the CRUD service for our entity.
     * 
     * @param entityClass the entity class
     * @return the CRUD service
     */
    public static CrudService<?, ?> getService(Class<?> entityClass) {
        return SERVICES.get(entityClass);
    }
    
    /**
     * Retrieve the CRUD repository for our entity.
     * 
     * @param entityClass the entity class
     * @return the CRUD repository
     */
    public static PagingAndSortingRepository<?, ?> getRepository(Class<?> entityClass) {
        return REPOSITORIES.get(entityClass);
    }
    
    /**
     * Register a new service instance.
     * 
     * @param entityClass the entity class
     * @param instance the instance
     */
    public static void register(Class<?> entityClass, CrudService<?, ?> instance) {
        SERVICES.put(entityClass, instance);
    }
    
    /**
     * Register a new repository instance.
     * 
     * @param entityClass the entity class
     * @param instance the instance
     */
    public static void register(Class<?> entityClass, PagingAndSortingRepository<?, ?> instance) {
        REPOSITORIES.put(entityClass, instance);
    }

}
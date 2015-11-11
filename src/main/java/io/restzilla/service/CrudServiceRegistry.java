/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Registers all service and repository instances per entity class.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class CrudServiceRegistry {

    private final Map<Class<?>, PagingAndSortingRepository<?, ?>> repositories;
    
    private final Map<Class<?>, CrudService<?, ?>> services;
    
    private final CrudServiceFactory factory;

    {
        repositories = new HashMap<Class<?>, PagingAndSortingRepository<?, ?>>();
        services = new HashMap<Class<?>, CrudService<?, ?>>();
    }

    public CrudServiceRegistry(CrudServiceFactory factory) {
        this.factory = factory;
    }

    /**
     * Retrieve all entity classes.
     * 
     * @return the entity classes
     */
    public Set<Class<?>> getEntityClasses() {
        return services.keySet();
    }

    /**
     * Retrieve the service for our entity. When no instance can be found we
     * initialize and register a new service bean.
     * 
     * @param entityClass the entity class
     * @return the service bean
     */
    @SuppressWarnings("unchecked")
    public <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> getService(Class<T> entityClass) {
        CrudService<T, ID> service = (CrudService<T, ID>) services.get(entityClass);
        if (service == null) {
            service = generateService(entityClass);
        }
        return service;
    }
    
    /**
     * Retrieve the repository for our entity. When no instance can be found we
     * initialize and register a new repository bean.
     * 
     * @param entityClass the entity class
     * @return the repository bean
     */
    @SuppressWarnings("unchecked")
    public <T extends Persistable<ID>, ID extends Serializable> PagingAndSortingRepository<T, ID> getRepository(Class<T> entityClass) {
        PagingAndSortingRepository<T, ID> repository = (PagingAndSortingRepository<T, ID>) repositories.get(entityClass);
        if (repository == null) {
            repository = generateRepository(entityClass);
        }
        return repository;
    }
    
    /**
     * Register a new service instance.
     * 
     * @param entityClass the entity class
     * @param service the instance
     */
    @SuppressWarnings("unchecked")
    <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> registerService(Class<T> entityClass, CrudService<T, ID> service) {
        services.put(entityClass, service);
        // Hook that automatically wires the repository into a custom service
        if (service instanceof RepositoryAware) {
            autowireRepository(entityClass, (RepositoryAware<T, ID>) service);
        }
        return service;
    }
    
    private <T extends Persistable<ID>, ID extends Serializable> void autowireRepository(Class<T> entityClass, RepositoryAware<T, ID> instance) {
        if (instance.getRepository() == null) {
            instance.setRepository(getRepository(entityClass));
        }
    }

    /**
     * Generates a new service instance.
     * 
     * @param entityClass the entity class
     * @param repository the repository instance
     */
    <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> generateService(Class<T> entityClass) {
        PagingAndSortingRepository<T, ID> repository = getRepository(entityClass);
        return generateService(entityClass, repository);
    }
    
    /**
     * Generates a new service instance.
     * 
     * @param entityClass the entity class
     * @param repository the repository instance
     */
    <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> generateService(Class<T> entityClass, PagingAndSortingRepository<T, ID> repository) {
        CrudService<T, ID> service = factory.buildService(entityClass, repository);
        return registerService(entityClass, service);
    }
    
    /**
     * Register a new repository instance.
     * 
     * @param entityClass the entity class
     * @param repository the instance
     */
    <T, ID extends Serializable> PagingAndSortingRepository<T, ID> registerRepository(Class<?> entityClass, PagingAndSortingRepository<T, ID> repository) {
        repositories.put(entityClass, repository);
        return repository;
    }
    
    /**
     * Generates a new repository instance.
     * 
     * @param entityClass the entity class
     */
    <T extends Persistable<ID>, ID extends Serializable> PagingAndSortingRepository<T, ID> generateRepository(Class<T> entityClass) {
        PagingAndSortingRepository<T, ID> repository = factory.buildRepository(entityClass);
        return registerRepository(entityClass, repository);
    }

}
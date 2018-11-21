/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.registry;

import io.restzilla.service.CrudService;

import java.io.Serializable;

import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Automatically generating map based implementation of {@link CrudServiceRegistry}.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
class CachingServiceRegistry extends MapCrudServiceRegistry {

    private final CrudServiceFactory factory;

    public CachingServiceRegistry(CrudServiceFactory factory) {
        this.factory = factory;
    }

    // Lookup

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> getService(Class<T> entityClass) {
        CrudService<T, ID> service = super.getService(entityClass);
        if (service == null) {
            service = generateService(entityClass);
        }
        return service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Persistable<ID>, ID extends Serializable> PagingAndSortingRepository<T, ID> getRepository(Class<T> entityClass) {
        PagingAndSortingRepository<T, ID> repository = super.getRepository(entityClass);
        if (repository == null) {
            repository = generateRepository(entityClass);
        }
        return repository;
    }
    
    // Registration

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> registerService(Class<T> entityClass, CrudService<T, ID> service) {
        // Hook that automatically wires the repository into a custom service
        if (service instanceof RepositoryAware) {
            autowireRepository(entityClass, (RepositoryAware<T, ID>) service);
        }
        
        return super.registerService(entityClass, service);
    }
    
    private <T extends Persistable<ID>, ID extends Serializable> void autowireRepository(Class<T> entityClass, RepositoryAware<T, ID> instance) {
        if (instance.getRepository() == null) {
            instance.setRepository(getRepository(entityClass));
        }
    }

    // Generation
    
    /**
     * Generates a new service instance.
     * 
     * @param entityClass the entity class
     */
    private <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> generateService(Class<T> entityClass) {
        PagingAndSortingRepository<T, ID> repository = getRepository(entityClass);
        CrudService<T, ID> service = factory.buildService(entityClass, repository);
        return registerService(entityClass, service);
    }

    private <T extends Persistable<ID>, ID extends Serializable> PagingAndSortingRepository<T, ID> generateRepository(Class<T> entityClass) {
        PagingAndSortingRepository<T, ID> repository = factory.buildRepository(entityClass);
        return registerRepository(entityClass, repository);
    }

}
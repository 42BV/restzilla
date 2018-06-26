package io.restzilla.registry;

import io.restzilla.service.CrudService;
import io.restzilla.service.CrudServiceRegistry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Map based implementation of {@link CrudServiceRegistry}.
 *
 * @author Jeroen van Schagen
 * @since May 12, 2016
 */
public class MapCrudServiceRegistry implements CrudServiceRegistry {
    
    private final Map<Class<?>, PagingAndSortingRepository<?, ?>> repositories;
    private final Map<Class<?>, CrudService<?, ?>> services;
    
    {
        repositories = new HashMap<>();
        services = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> getService(Class<T> entityClass) {
        return (CrudService<T, ID>) services.get(entityClass);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Persistable<ID>, ID extends Serializable> PagingAndSortingRepository<T, ID> getRepository(Class<T> entityClass) {
        return (PagingAndSortingRepository<T, ID>) repositories.get(entityClass);
    }
    
    /**
     * Register a new service instance.
     * 
     * @param <T> the entity type
     * @param <ID> the identifier type
     * @param entityClass the entity class
     * @param service the instance
     * @return the registered service
     */
    @SuppressWarnings("unchecked")
    public <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> registerService(Class<T> entityClass, CrudService<T, ID> service) {
        services.put(entityClass, service);
        return service;
    }
    
    /**
     * Register a new repository instance.
     * 
     * @param <T> the entity type
     * @param <ID> the identifier type
     * @param entityClass the entity class
     * @param repository the instance
     * @return the registered repository
     */
    public <T, ID extends Serializable> PagingAndSortingRepository<T, ID> registerRepository(Class<?> entityClass, PagingAndSortingRepository<T, ID> repository) {
        repositories.put(entityClass, repository);
        return repository;
    }

}

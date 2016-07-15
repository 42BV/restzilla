package io.restzilla.service;

import java.io.Serializable;

import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 
 *
 * @author jeroen
 * @since May 12, 2016
 */
public interface CrudServiceRegistry {
    
    /**
     * Retrieve the service for our entity. When no instance can be found we
     * initialize and register a new service bean.
     * 
     * @param <T> the entity type
     * @param <ID> the identifier type
     * @param entityClass the entity class
     * @return the service bean
     */
    <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> getService(Class<T> entityClass);
    
    /**
     * Retrieve the repository for our entity. When no instance can be found we
     * initialize and register a new repository bean.
     * 
     * @param <T> the entity type
     * @param <ID> the identifier type
     * @param entityClass the entity class
     * @return the repository bean
     */
    <T extends Persistable<ID>, ID extends Serializable> PagingAndSortingRepository<T, ID> getRepository(Class<T> entityClass);

}

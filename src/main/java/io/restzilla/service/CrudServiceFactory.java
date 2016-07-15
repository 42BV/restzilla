/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service;

import java.io.Serializable;

import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Generates missing services and/or repositories.
 *
 * @author Jeroen van Schagen
 * @since Sep 18, 2015
 */
public interface CrudServiceFactory {
    
    /**
     * Build a new repository.
     * 
     * @param <T> the entity type
     * @param <ID> the identifier type
     * @param entityClass the entity class
     * @return the repository
     */
    <T extends Persistable<ID>, ID extends Serializable> PagingAndSortingRepository<T, ID> buildRepository(Class<T> entityClass);

    /**
     * Build a new service.
     * 
     * @param <T> the entity type
     * @param <ID> the identifier type
     * @param entityClass the entity class
     * @param repository the underlying repository
     * @return the service
     */
    <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> buildService(Class<T> entityClass, PagingAndSortingRepository<T, ID> repository);

}

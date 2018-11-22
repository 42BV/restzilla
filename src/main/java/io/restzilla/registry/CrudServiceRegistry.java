package io.restzilla.registry;

import java.io.Serializable;

import io.restzilla.service.CrudService;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Registry capable of retrieving a CRUD service or repository
 * based on the entity class.
 *
 * @author jeroen
 * @since May 12, 2016
 */
public interface CrudServiceRegistry {
    
    /**
     * Retrieve the CRUD service of an entity.
     * 
     * @param <T> the entity type
     * @param <ID> the identifier type
     * @param entityClass the entity class
     * @return the service bean
     */
    <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> getService(Class<T> entityClass);
    
    /**
     * Retrieve the CRUD repository of an entity.
     * 
     * @param <T> the entity type
     * @param <ID> the identifier type
     * @param entityClass the entity class
     * @return the repository bean
     */
    <T extends Persistable<ID>, ID extends Serializable> PagingAndSortingRepository<T, ID> getRepository(Class<T> entityClass);

}

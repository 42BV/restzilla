/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.service;

import java.io.Serializable;
import java.util.List;

import nl._42.restzilla.registry.CrudServiceRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;

import com.google.common.base.Preconditions;

/**
 * Service capable of reading any type of entity.
 * <br><br>
 * <b>This service is strictly for internal usage and should
 * not be exposed to the outside.</b>
 *
 * @author Jeroen van Schagen
 * @since Nov 6, 2015
 */
public class ReadService {
    
    /**
     * Registry containing all service instanced. Note that this
     * variable is not final as it can be injected dynamically.
     */
    private final CrudServiceRegistry crudServiceRegistry;
    
    public ReadService(CrudServiceRegistry crudServiceRegistry) {
        this.crudServiceRegistry = Preconditions.checkNotNull(crudServiceRegistry, "Service registry is required.");
    }

    /**
     * Retrieve all entities with a certain sort.
     * 
     * @param <T> the entity type
     * @param <ID> the identifier type
     * @param entityClass the entity class
     * @param sort the sorting
     * @return the sorted entities
     */
    public <T extends Persistable<ID>, ID extends Serializable> List<T> findAll(Class<T> entityClass, Sort sort) {
        return getService(entityClass).findAll(sort);
    }

    /**
     * Retrieve a page of entities.
     * 
     * @param <T> the entity type
     * @param <ID> the identifier type
     * @param entityClass the entity class
     * @param pageable the pageable
     * @return the page of entities
     */
    public <T extends Persistable<ID>, ID extends Serializable> Page<T> findAll(Class<T> entityClass, Pageable pageable) {
        return getService(entityClass).findAll(pageable);
    }

    /**
     * Retrieve a specific entity, with an identifier.
     * 
     * @param <T> the entity type
     * @param <ID> the identifier type
     * @param entityClass the entity class
     * @param id the identifier
     * @return the result entity, if any
     */
    public <T extends Persistable<ID>, ID extends Serializable> T getOne(Class<T> entityClass, ID id) {
        return getService(entityClass).getOne(id);
    }
    
    private <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> getService(Class<T> entityClass) {
        return crudServiceRegistry.getService(entityClass);
    }

}

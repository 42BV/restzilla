package io.restzilla.web.query;

import io.restzilla.service.CrudService;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;

/**
 * Adapts the {@link CrudService} to the {@link Listable} interface.
 * Service does not directly implements interface to prevent the getEntityClass to be mocked on each occasion.
 *
 * @author Jeroen van Schagen
 * @since May 12, 2016
 */
public class CrudServiceListable<T extends Persistable<ID>, ID extends Serializable> implements Listable<T> {
    
    private final CrudService<T, ID> crudService;
    
    private final Class<T> entityClass;

    public CrudServiceListable(CrudService<T, ID> crudService, Class<T> entityClass) {
        this.crudService = crudService;
        this.entityClass = entityClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> findAll(Sort sort) {
        return crudService.findAll(sort);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Page<T> findAll(Pageable pageable) {
        return crudService.findAll(pageable);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getEntityClass() {
        return entityClass;
    }
    
}

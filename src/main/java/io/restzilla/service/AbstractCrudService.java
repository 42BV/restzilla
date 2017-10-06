package io.restzilla.service;

import io.beanmapper.spring.Lazy;

import java.io.Serializable;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Persistable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

public abstract class AbstractCrudService<T extends Persistable<ID>, ID extends Serializable> implements CrudService<T, ID> {
    
    private final Class<T> entityClass;
    
    /**
     * Construct a new service.
     * <br>
     * <b>This constructor dynamically resolves the entity type with reflection.</b>
     */
    @SuppressWarnings("unchecked")
    public AbstractCrudService() {
        this.entityClass = (Class<T>) resolveEntityType();
        Assert.notNull(entityClass, "Entity class cannot be null");
    }
    
    /**
     * Dynamically resolve the entity type based on generic type arguments.
     * 
     * @return the resolved entity type
     */
    private Class<?> resolveEntityType() {
        return GenericTypeResolver.resolveTypeArguments(getClass(), AbstractCrudService.class)[0];
    }
    
    /**
     * Construct a new service.
     * 
     * @param entityClass the entity class
     */
    public AbstractCrudService(Class<T> entityClass) {
        Assert.notNull(entityClass, "Entity class cannot be null");
        this.entityClass = entityClass;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<T> find(ID id) {
        T result = findOne(id);
        return Optional.ofNullable(result);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public T getOne(ID id) {
        T entity = findOne(id);
        if (entity == null) {
            throw new EntityNotFoundException("Could not find entity '" + getEntityClass().getSimpleName() + "' with id: " + id);
        }
        return entity;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends T> S save(Lazy<S> entity) {
        return save(entity.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }
    
}

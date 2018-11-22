package nl._42.restzilla.service;

import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Persistable;
import org.springframework.util.Assert;

import java.io.Serializable;

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
    public Class<T> getEntityClass() {
        return entityClass;
    }
    
}

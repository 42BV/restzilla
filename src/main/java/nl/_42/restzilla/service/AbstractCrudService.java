package nl._42.restzilla.service;

import nl._42.restzilla.repository.RepositoryAware;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

public abstract class AbstractCrudService<T extends Persistable<ID>, ID extends Serializable> implements CrudService<T, ID>, RepositoryAware<T, ID> {

    /**
     * Repository used to communicate with the database. Note that
     * this instance is not marked as final, because it can be
     * dynamically injected at runtime.
     */
    private PagingAndSortingRepository<T, ID> repository;

    private final Class<T> entityClass;

    /**
     * Construct a new service.
     * <br>
     * <b>This constructor dynamically resolves the entity type with reflection.</b>
     */
    @SuppressWarnings("unchecked")
    public AbstractCrudService() {
        this.entityClass = (Class<T>) resolveEntityType();
        requireNonNull(entityClass, "Entity class cannot be null");
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
        requireNonNull(entityClass, "Entity class cannot be null");
        this.entityClass = entityClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PagingAndSortingRepository<T, ID> getRepository() {
        return repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRepository(PagingAndSortingRepository<T, ID> repository) {
        this.repository = repository;
    }

}

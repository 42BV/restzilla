package nl._42.restzilla.service;

import nl._42.restzilla.repository.RepositoryAware;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;

public abstract class AbstractCrudService<T extends Persistable<ID>, ID extends Serializable> implements CrudService<T, ID>, RepositoryAware<T, ID> {

    /**
     * Repository used to communicate with the database. Note that
     * this instance is not marked as final, because it can be
     * dynamically injected at runtime.
     */
    private JpaRepository<T, ID> repository;

    /**
     * {@inheritDoc}
     */
    @Override
    public JpaRepository<T, ID> getRepository() {
        return repository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRepository(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

}

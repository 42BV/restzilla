/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.registry;

import io.restzilla.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;
import java.util.List;

/**
 * Automatically generating map based implementation of {@link CrudServiceRegistry}.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
class DefaultServiceRegistry
  extends AbstractServiceRegistry
  implements CrudServiceRegistry {

    private final CrudServiceFactory factory;

    public DefaultServiceRegistry(final CrudServiceFactory factory) {
        this.factory = factory;
    }

    // Lookup

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> getService(Class<T> entityClass) {
        return serviceOf(
          entityClass,
          this::generateService
        );
    }

    private <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> generateService(Class<T> entityClass) {
        PagingAndSortingRepository<T, ID> repository = getRepository(entityClass);
        return factory.buildService(entityClass, repository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Persistable<ID>, ID extends Serializable> PagingAndSortingRepository<T, ID> getRepository(Class<T> entityClass) {
        return repositoryOf(
          entityClass,
          factory::buildRepository
        );
    }

    // Registration

    @Autowired(required = false)
    public void setRepositories(List<PagingAndSortingRepository<?, ?>> repositories) {
        repositories.forEach(this::registerRepository);
    }

    @Autowired(required = false)
    public void setServices(List<CrudService<?, ?>> services) {
        services.forEach(this::registerService);
    }

    @Override
    protected void registerService(CrudService<?, ?> service) {
        if (service instanceof RepositoryAware) {
            autowireRepository(service.getEntityClass(), (RepositoryAware) service);
        }

        super.registerService(service);
    }

    // Hook that automatically wires the repository into a custom service
    private <T extends Persistable<ID>, ID extends Serializable> void autowireRepository(
      final Class<T> entityClass,
      final RepositoryAware<T, ID> service
    ) {

        if (service.getRepository() == null) {
            service.setRepository(getRepository(entityClass));
        }
    }

}

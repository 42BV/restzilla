/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.registry;

import nl._42.restzilla.repository.CrudRepositoryFactory;
import nl._42.restzilla.service.CrudService;
import nl._42.restzilla.service.CrudServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.List;

/**
 * Default service registry implementation.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
class DefaultServiceRegistry extends AbstractServiceRegistry implements CrudServiceRegistry {

    private final CrudRepositoryFactory repositoryFactory;

    private final CrudServiceFactory serviceFactory;

    DefaultServiceRegistry(CrudRepositoryFactory repositoryFactory, CrudServiceFactory serviceFactory) {
        this.repositoryFactory = repositoryFactory;
        this.serviceFactory = serviceFactory;
    }

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
        return serviceFactory.build(
          entityClass,
          getRepository(entityClass)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Persistable<ID>, ID extends Serializable> JpaRepository<T, ID> getRepository(Class<T> entityClass) {
        return repositoryOf(entityClass, repositoryFactory::build);
    }

    @Autowired(required = false)
    public void setRepositories(List<JpaRepository<?, ?>> repositories) {
        repositories.forEach(this::registerRepository);
    }

    @Autowired(required = false)
    public void setServices(List<CrudService<?, ?>> services) {
        services.forEach(this::registerService);
    }

}

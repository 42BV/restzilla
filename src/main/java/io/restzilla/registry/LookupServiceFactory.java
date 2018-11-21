/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.registry;

import io.restzilla.service.CrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service factory that lazy retrieves from the application context.
 * By lazy retrieval you can be certain that all beans are registered before retrieval.
 *
 * @author Jeroen van Schagen
 * @since Dec 10, 2015
 */
@SuppressWarnings("unchecked")
class LookupServiceFactory implements CrudServiceFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LookupServiceFactory.class);

    private final Map<Class<?>, PagingAndSortingRepository<?, ?>> repositories = new HashMap<>();

    private final Map<Class<?>, CrudService<?, ?>> services = new HashMap<>();
    
    private final CrudServiceFactory delegate;

    public LookupServiceFactory(CrudServiceFactory delegate) {
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Persistable<ID>, ID extends Serializable> PagingAndSortingRepository<T, ID> buildRepository(Class<T> entityClass) {
        PagingAndSortingRepository<?, ?> repository = repositories.get(entityClass);
        if (repository != null) {
            return (PagingAndSortingRepository<T, ID>) repository;
        } else {
            LOGGER.debug("Generating repository for {} as none is defined.", entityClass);
            return delegate.buildRepository(entityClass);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> buildService(Class<T> entityClass, PagingAndSortingRepository<T, ID> repository) {
        CrudService<?, ?> service = services.get(entityClass);
        if (service != null) {
            return (CrudService<T, ID>) service;
        } else {
            LOGGER.debug("Generating service for {} as none is defined.", entityClass);
            return delegate.buildService(entityClass, repository);
        }
    }

    @Autowired(required = false)
    public void setRepositories(List<PagingAndSortingRepository<?, ?>> repositories) {
        this.repositories.clear();
        repositories.forEach(repository -> {
            Class<?>[] arguments = GenericTypeResolver.resolveTypeArguments(repository.getClass(), PagingAndSortingRepository.class);
            if (arguments != null && arguments.length == 2) {
                this.repositories.put(arguments[0], repository);
            }
        });
    }

    @Autowired(required = false)
    public void setServices(List<CrudService<?, ?>> services) {
        this.services.clear();
        services.forEach(service -> this.services.put(service.getEntityClass(), service));
    }

}

package io.restzilla.registry;

import io.restzilla.service.CrudService;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Default implementation of a service registry that uses concurrent
 * hash maps for caching.
 */
public abstract class AbstractServiceRegistry
  implements CrudServiceRegistry {

    private final ConcurrentHashMap<Class<?>, PagingAndSortingRepository<?, ?>> repositories =
      new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Class<?>, CrudService<?, ?>> services =
      new ConcurrentHashMap<>();

    protected <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> serviceOf(
      final Class<T> entityClass,
      final Function<Class<T>, CrudService<T, ID>> factory
    ) {

        CrudService<?, ?> service = services.computeIfAbsent(
          entityClass,
          (type) -> factory.apply(entityClass)
        );

        return (CrudService<T, ID>) service;
    }

    protected <T extends Persistable<ID>, ID extends Serializable> PagingAndSortingRepository<T, ID> repositoryOf(
      final Class<T> entityClass,
      final Function<Class<T>, PagingAndSortingRepository<T, ID>> factory
    ) {

        PagingAndSortingRepository<?, ?> service = repositories.computeIfAbsent(
          entityClass,
          (type) -> factory.apply(entityClass)
        );

        return (PagingAndSortingRepository<T, ID>) service;
    }

    protected void registerRepository(PagingAndSortingRepository<?, ?> repository) {
        Class<?>[] arguments = GenericTypeResolver.resolveTypeArguments(repository.getClass(), PagingAndSortingRepository.class);
        if (arguments != null && arguments.length == 2) {
            this.registerRepository(arguments[0], repository);
        }
    }

    protected void registerRepository(Class<?> argument, PagingAndSortingRepository<?, ?> repository) {
        this.repositories.put(argument, repository);
    }

    protected void registerService(CrudService<?, ?> service) {
        this.registerService(service.getEntityClass(), service);
    }

    protected void registerService(Class<?> entityClass, CrudService<?, ?> service) {
        this.services.put(entityClass, service);
    }

}

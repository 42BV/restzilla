package nl._42.restzilla.registry;

import nl._42.restzilla.repository.RepositoryAware;
import nl._42.restzilla.service.CrudService;
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
abstract class AbstractServiceRegistry implements CrudServiceRegistry {

    private final ConcurrentHashMap<Class<?>, PagingAndSortingRepository<?, ?>> repositories =
      new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Class<?>, CrudService<?, ?>> services =
      new ConcurrentHashMap<>();

    /**
     * Retrieve or build the service.
     *
     * @param entityClass the entity class
     * @param factory the factory method
     * @param <T> entity type
     * @param <ID> entity identifier type
     * @return the service
     */
    @SuppressWarnings("unchecked")
    <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> serviceOf(
      final Class<T> entityClass,
      final Function<Class<T>, CrudService<T, ID>> factory
    ) {

        CrudService<?, ?> service = services.computeIfAbsent(
          entityClass,
          (type) -> factory.apply(entityClass)
        );

        return (CrudService<T, ID>) service;
    }

    /**
     * Retrieve or build the repository.
     *
     * @param entityClass the entity class
     * @param factory the factory method
     * @param <T> entity type
     * @param <ID> entity identifier type
     * @return the repository
     */
    @SuppressWarnings("unchecked")
    <T extends Persistable<ID>, ID extends Serializable> PagingAndSortingRepository<T, ID> repositoryOf(
      final Class<T> entityClass,
      final Function<Class<T>, PagingAndSortingRepository<T, ID>> factory
    ) {

        PagingAndSortingRepository<?, ?> service = repositories.computeIfAbsent(
          entityClass,
          (type) -> factory.apply(entityClass)
        );

        return (PagingAndSortingRepository<T, ID>) service;
    }

    /**
     * Register a new repository.
     *
     * @param repository the repository
     */
    void registerRepository(PagingAndSortingRepository<?, ?> repository) {
        Class<?>[] arguments = GenericTypeResolver.resolveTypeArguments(repository.getClass(), PagingAndSortingRepository.class);
        if (arguments != null && arguments.length == 2) {
            this.registerRepository(arguments[0], repository);
        }
    }

    /**
     * Register a new repository.
     *
     * @param entityClass the entity class
     * @param repository the repository
     */
    private void registerRepository(Class<?> entityClass, PagingAndSortingRepository<?, ?> repository) {
        this.repositories.put(entityClass, repository);
    }

    /**
     * Register a new service.
     *
     * @param service the service
     */
    void registerService(CrudService<?, ?> service) {
        this.registerService(service.getEntityClass(), service);
    }

    /**
     * Register a new service.
     *
     * @param entityClass the entity class
     * @param service the service
     */
    private void registerService(Class<?> entityClass, CrudService<?, ?> service) {
        if (service instanceof RepositoryAware) {
            ((RepositoryAware) service).setRepository(
              getRepository(service.getEntityClass())
            );
        }

        this.services.put(entityClass, service);
    }

}

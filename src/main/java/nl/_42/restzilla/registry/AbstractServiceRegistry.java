package nl._42.restzilla.registry;

import nl._42.restzilla.repository.RepositoryAware;
import nl._42.restzilla.service.CrudService;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Default implementation of a service registry that uses concurrent
 * hash maps for caching.
 */
abstract class AbstractServiceRegistry implements CrudServiceRegistry {

    private final ConcurrentHashMap<Class<?>, JpaRepository<?, ?>> repositories = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, CrudService<?, ?>> services = new ConcurrentHashMap<>();

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
    <T extends Persistable<ID>, ID extends Serializable> JpaRepository<T, ID> repositoryOf(
      final Class<T> entityClass,
      final Function<Class<T>, JpaRepository<T, ID>> factory
    ) {

        JpaRepository<?, ?> service = repositories.computeIfAbsent(
          entityClass,
          (type) -> factory.apply(entityClass)
        );

        return (JpaRepository<T, ID>) service;
    }

    /**
     * Register a new repository.
     *
     * @param repository the repository
     */
    void registerRepository(JpaRepository<?, ?> repository) {
      getEntityClass(repository, JpaRepository.class).ifPresent(entityClass ->
          registerRepository(entityClass, repository)
      );
    }

    /**
     * Register a new repository.
     *
     * @param entityClass the entity class
     * @param repository the repository
     */
    private void registerRepository(Class<?> entityClass, JpaRepository<?, ?> repository) {
        this.repositories.put(entityClass, repository);
    }

    /**
     * Register a new service.
     *
     * @param service the service
     */
    void registerService(CrudService<?, ?> service) {
        getEntityClass(service, CrudService.class).ifPresent(entityClass ->
          this.registerService(entityClass, service)
        );
    }

    /**
     * Register a new service.
     *
     * @param entityClass the entity class
     * @param service the service
     */
    @SuppressWarnings("unchecked")
    private void registerService(Class<?> entityClass, CrudService<?, ?> service) {
        if (service instanceof RepositoryAware repositoryService) {
            JpaRepository repository = getRepository((Class) entityClass);
            repositoryService.setRepository(repository);
        }

        this.services.put(entityClass, service);
    }

    private Optional<Class<?>> getEntityClass(Object container, Class<?> interfaceType) {
        Class<?> entityClass = null;

        if (container instanceof EntityClassAware) {
            entityClass = ((EntityClassAware) container).getEntityClass();
        } else {
            Class<?>[] arguments = GenericTypeResolver.resolveTypeArguments(container.getClass(), interfaceType);
            if (arguments != null && arguments.length == 2) {
                entityClass = arguments[0];
            }
        }

        return Optional.ofNullable(entityClass);
    }

}

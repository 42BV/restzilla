package nl._42.restzilla.service;

import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;

@FunctionalInterface
public interface CrudServiceFactory {

    /**
     * Build a new service based on an entity class.
     *
     * @param entityClass the entity class
     * @param repository the repository to use
     * @param <T> the entity type
     * @param <ID> the identifier type
     * @return the service
     */
    <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> build(
      final Class<T> entityClass,
      final JpaRepository<T, ID> repository
    );

}

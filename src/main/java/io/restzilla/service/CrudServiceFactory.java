package io.restzilla.service;

import io.restzilla.service.CrudService;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;

@FunctionalInterface
public interface CrudServiceFactory {

    /**
     * Build a new service based on an entity class.
     *
     * @param entityClass the entity class
     * @param <T> the entity type
     * @param <ID> the identifier type
     * @return the service
     */
    <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> build(
      final Class<T> entityClass,
      final PagingAndSortingRepository<T, ID> repository
    );

}

package nl._42.restzilla.repository;

import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;

@FunctionalInterface
public interface CrudRepositoryFactory {

    /**
     * Build a new repository based on an entity class.
     *
     * @param entityClass the entity class
     * @param <T> the entity type
     * @param <ID> the identifier type
     * @return the repository
     */
    <T extends Persistable<ID>, ID extends Serializable> PagingAndSortingRepository<T, ID> build(
      final Class<T> entityClass
    );

}

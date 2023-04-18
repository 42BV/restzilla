package nl._42.restzilla.service;

import com.google.common.collect.Lists;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Service capable of retrieving specific and collections
 * of a certain entity type.
 * @param <T> the entity type
 * @param <ID> the identifier type
 */
public interface PagingAndSortingService<T extends Persistable<ID>, ID extends Serializable> {

  /**
   * Returns all entities.
   *
   * @return all entities
   */
  @Transactional(readOnly = true)
  default List<T> findAll() {
    return (List<T>) getRepository().findAll();
  }

  /**
   * Returns all entities matching the specified IDs
   * @param ids IDs to find the entities for.
   * @return All found entities.
   */
  @Transactional(readOnly = true)
  default List<T> findAll(Iterable<ID> ids) {
    return Lists.newArrayList(getRepository().findAllById(ids));
  }

  /**
   * Returns all entities, sorted.
   *
   * @param sort the sort
   * @return all entities
   */
  @Transactional(readOnly = true)
  default List<T> findAll(Sort sort) {
    return (List<T>) getRepository().findAll(sort);
  }

  /**
   * Returns a page of entities.
   *
   * @param pageable the pageable
   * @return the entities in that page
   */
  @Transactional(readOnly = true)
  default Page<T> findAll(Pageable pageable) {
    return getRepository().findAll(pageable);
  }

  /**
   * Retrieve an optional entity based on its identifier.
   *
   * @param id the identifier
   * @return the optional entity
   */
  @Transactional(readOnly = true)
  default Optional<T> find(ID id) {
    if (id == null) {
      return Optional.empty();
    }

    return getRepository().findById(id);
  }

  /**
   * Retrieves an entity by its id.
   *
   * @param id must not be {@literal null}.
   * @return the entity with the given id or {@literal null} if none found
   * @throws IllegalArgumentException if {@code id} is {@literal null}
   */
  @Transactional(readOnly = true)
  default T findOne(ID id) {
    return find(id).orElse(null);
  }

  /**
   * Retrieves an entity by its id, but when the value is null we throw an exception.
   *
   * @param id must not be {@literal null}.
   * @return the entity with the given id
   * @throws EntityNotFoundException if the result cannot be found
   */
  @Transactional(readOnly = true)
  default T getOne(ID id) {
    return find(id).orElseThrow(
      () -> new EntityNotFoundException(
        format("Cannot find entity with identifier: %s", id)
      )
    );
  }

  /**
   * Retrieve the underlying repository.
   * @return the repository
   */
  JpaRepository<T, ID> getRepository();

}

package nl._42.restzilla.registry;

/**
 * Components that implement this interface are aware
 * of the type of entity classes they manage.
 * @param <T> the type of entities
 */
public interface EntityClassAware<T> {

  /**
   * Retrieve the entity class.
   *
   * @return the entity class
   */
  Class<T> getEntityClass();

}

package nl._42.restzilla.service;

/**
 * Interface for lazily retrieving a value.
 * @param <T> the value type
 */
@FunctionalInterface
public interface Lazy<T> {

  /**
   * Retrieve the current value.
   * @return the value
   * @throws Exception an exception when the value cannot be retrieved
   */
  T get() throws Exception;

  /**
   * Retrieve the current value and wrap any checked exceptions into a runtime exception.
   * @return the value
   */
  default T apply() {
    try {
      return get();
    } catch (RuntimeException rte) {
      throw rte;
    } catch (Exception e) {
      throw new LazyRetrievalException(e);
    }
  }

  /**
   * Exception thrown when a checked exception has to be wrapped.
   */
  class LazyRetrievalException extends RuntimeException {

    public LazyRetrievalException(final Exception e) {
      super(e);
    }

  }

}

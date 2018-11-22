package nl._42.restzilla.service;

/**
 * LazyEntityRetrievalException is a runtime exception which is thrown when the underlying BeanMapper failed to obtain a Lazy entity
 * In such case, we are usually outside of the Hibernate session in which the Lazy wrapper was retrieved.
 */
public class LazyEntityRetrievalException extends RuntimeException {

    public LazyEntityRetrievalException(Exception e) {
        super(e);
    }

}

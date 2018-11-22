package nl._42.restzilla.web.query;

public interface Finder<T> {
    
    /**
     * Retrieve a single result.
     * 
     * @return the single result, or {@code null}
     */
    T findOne();

}

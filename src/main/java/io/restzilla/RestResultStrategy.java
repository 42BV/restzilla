package io.restzilla;

/**
 * Strategies for retrieving the result.
 *
 * @author Jeroen van Schagen
 * @since Nov 6, 2015
 */
public enum RestResultStrategy {
    
    /**
     * Convert the entity into it's result type using reflection
     * based bean mapping.
     */
    MAPPING,
    
    /**
     * Retrieve the result by database query.
     */
    QUERY;

}

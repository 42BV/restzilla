/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Configuration of a custom REST find query.
 *
 * @author Jeroen van Schagen
 * @since Dec 9, 2015
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface RestQuery {

    /**
     * Parameters that our request must contain
     * 
     * @return the parameter names
     */
    String[] parameters();
    
    /**
     * Name of the repository finder method that must be used.
     * 
     * @return the method
     */
    String method();
    
    /**
     * Alternate entity type where the query can be found.
     * 
     * @return the entity type
     */
    Class<?> entityType() default Object.class;

    /**
     * Alternate result type where the query can be found.
     * 
     * @return the result type
     */
    Class<?> resultType() default Object.class;
    
    /**
     * Determines if the query returns in a single result.
     * 
     * @return {@code true} when single result, else {@code false}
     */
    boolean unique() default false;

}

/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Automatically generates a REST entpoint for this entity,
 * providing the following functionality:
 * 
 * <br>
 * <ul>
 * <li><b>GET /</b> returns all entities</li>
 * <li><b>GET /{id}</b> returns entity with id</li>
 * <li><b>POST /</b> creates a new entity</li>
 * <li><b>PUT /</b> updates an existing entity</li>
 * <li><b>DELETE /{id}</b> deletes an entity with id</li>
 * </ul>
 * <br>
 * 
 * Functionality can be overwritten on each of the architectural
 * layers: repository, service and controller.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface EnableRest {
    
    /**
     * (Optional) the base path, when empty we use the entity name.
     * @return the base path
     */
    String basePath() default "";
    
    /**
     * Determines if we should only handle {@code GET} requests.
     * @return the read only
     */
    boolean readOnly() default false;

    /**
     * (Optional) the result type, when left empty we return the full entity.
     * @return the result type
     */
    Class<?> resultType() default Object.class;
    
    /**
     * (Optional) the input creation type
     * @return the creation type
     */
    Class<?> createType() default Object.class;

    /**
     * (Optional) the input update type 
     * @return the update type
     */
    Class<?> updateType() default Object.class;

}

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
public @interface RestResource {
    
    /**
     * (Optional) the base path, when empty we use the entity name.
     * @return the base path
     */
    String basePath() default "";
    
    /**
     * (Optional) alternative entity class.
     * @return the entity class
     */
    Class<?> entityClass() default Object.class;
    
    /**
     * (Optional) the custom result type, when empty we just return the entity.
     * @return the result type
     */
    Class<?> resultType() default Object.class;
    
    /**
     * Whether the result value should be queried. Otherwise
     * we just perform a mapping.
     * @return if the result should be queried
     */
    boolean resultByQuery() default false;
    
    /**
     * Enable this if you only want to handle {@code GET} requests.
     * @return the read only
     */
    boolean readOnly() default false;
    
    /**
     * Enable this when our {@code getAll} should only return pages.
     * @return the paged only
     */
    boolean pagedOnly() default false;
    
    /**
     * Determines if update requests can be considered as patch.
     * When a patch occurs we only update the provided properties.
     * @return the patch
     */
    boolean patch() default true;
    
    /**
     * Finder queries that should be supported by our resource.
     * @return the queries
     */
    RestQuery[] queries() default {};

    // Function based configuration

    /**
     * (Optional) the configuration of our {@code findAll}
     * @return the configuration
     */
    RestConfig findAll() default @RestConfig;
    
    /**
     * (Optional) the configuration of our {@code findOne}
     * @return the configuration
     */
    RestConfig findOne() default @RestConfig;
    
    /**
     * (Optional) the configuration of our {@code create}
     * @return the configuration
     */
    RestConfig create() default @RestConfig;
    
    /**
     * (Optional) the configuration of our {@code update}
     * @return the configuration
     */
    RestConfig update() default @RestConfig;
    
    /**
     * (Optional) the configuration of our {@code delete}
     * @return the configuration
     */
    RestConfig delete() default @RestConfig;

}

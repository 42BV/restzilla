/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla;

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
     * (Optional) the entity type. When this annotation is placed on
     * the entity itself we fallback on that class.
     * @return the entity class
     */
    Class<?> entityType() default Object.class;
    
    /**
     * (Optional) the base path, when empty we use the entity name.
     * @return the base path
     */
    String basePath() default "";

    RestSecured secured() default @RestSecured;

    //
    // Query
    //

    /**
     * (Optional) the default query type. When undefined we use the entity type.
     * @return the query class
     */
    Class<?> queryType() default Object.class;

    /**
     * (Optional) the custom result type, when empty we just return the entity.
     * @return the result type
     */
    Class<?> resultType() default Object.class;
    
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
     * Finder queries that should be supported by our resource.
     * @return the queries
     */
    RestQuery[] queries() default {};
    
    //
    // Modification
    //

    /**
     * Determines if update requests can be considered as patch.
     * When a patch occurs we only update the provided properties.
     * @return the patch
     */
    boolean patch() default true;
    
    /**
     * (Optional) the custom input type. Default format for create/update calls,
     * when left empty we expect the entity type.
     * @return the input type
     */
    Class<?> inputType() default Object.class;

    //
    // Functions
    //

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

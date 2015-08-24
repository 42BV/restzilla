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
 * Annotating an entity with this will cause automatic
 * REST CRUD functionality to be registered.
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

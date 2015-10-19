/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.flyweight;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Configuration on a specific REST function.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface RestConfig {

    /**
     * Determines if this function is enabled.
     * @return the enabled
     */
    boolean enabled() default true;
    
    /**
     * (Optional) the input type, when left empty we expect the entity type
     * @return the input type
     */
    Class<?> inputType() default Object.class;

    /**
     * (Optional) the result type, when left empty we return the default result type
     * @return the result type
     */
    Class<?> resultType() default Object.class;
    
    /**
     * (Optional) the security that are allowed to perform this functionality
     * @return the security
     */
    String[] secured() default {};

}

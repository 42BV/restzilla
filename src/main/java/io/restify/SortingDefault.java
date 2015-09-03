/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.data.domain.Sort.Direction;

/**
 * Define the default sorting of an entity.
 *
 * @author Jeroen van Schagen
 * @since Sep 3, 2015
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface SortingDefault {

    /**
     * The properties to sort by by default.
     * 
     * @return
     */
    String[] value() default {};
    
    /**
     * The direction to sort by. Defaults to {@link Direction#ASC}.
     * 
     * @return
     */
    Direction direction() default Direction.ASC;

}

/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Security configuration on a REST resource.
 *
 * @author Jeroen van Schagen
 * @since Nov 23, 2018
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface RestSecured {

    /**
     * Optional security expression for both read and modifications.
     * @return the security expression
     */
    String[] value() default {};

    /**
     * Optional security expression for read operations.
     * @return the security expression
     */
    String[] read() default {};

    /**
     * (Optional) security expression for modifications.
     * @return the security expression
     */
    String[] modify() default {};

}

/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.util;

/**
 * Determines if a certain class is on the classpath.
 *
 * @author Jeroen van Schagen
 * @since Nov 12, 2015
 */
public class ClassUtil {
    
    /**
     * Determines if the class with the provided full name is
     * detected on our classpath.
     * 
     * @param className the full class name
     * @return {@code true} when found, else {@code false}
     */
    public static boolean isOnClasspath(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException cnfe) {
            return false;
        }
    }

}

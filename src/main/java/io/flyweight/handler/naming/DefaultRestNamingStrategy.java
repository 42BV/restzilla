/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.flyweight.handler.naming;

/**
 * Default implementation of the naming strategy.
 *
 * @author Jeroen van Schagen
 * @since Nov 10, 2015
 */
public class DefaultRestNamingStrategy implements RestNamingStrategy {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getBasePath(Class<?> entityClass) {
        return entityClass.getSimpleName().toLowerCase();
    }
    
}

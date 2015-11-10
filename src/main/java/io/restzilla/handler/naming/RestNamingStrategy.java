/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.handler.naming;

/**
 * Performs naming rules on our entity.
 *
 * @author Jeroen van Schagen
 * @since Nov 10, 2015
 */
public interface RestNamingStrategy {
    
    /**
     * Generates the base path for an entity.
     * 
     * @param entityClass the entity class
     * @return the base path
     */
    String getBasePath(Class<?> entityClass);
    
}

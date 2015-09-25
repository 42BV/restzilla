/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.handler.security;

import javax.servlet.http.HttpServletRequest;

/**
 * Determines if a user is authorized.
 *
 * @author Jeroen van Schagen
 * @since Sep 8, 2015
 */
public interface SecurityProvider {
    
    /**
     * Determine if a user is authorized.
     * 
     * @param roles the roles
     * @return {@code true} if authorized, else {@code false}
     */
    boolean isAuthorized(String[] roles, HttpServletRequest request);

}

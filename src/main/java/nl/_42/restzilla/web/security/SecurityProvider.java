/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.web.security;

import jakarta.servlet.http.HttpServletRequest;

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
     * @param request the request
     * @return {@code true} if authorized, else {@code false}
     */
    boolean isAuthorized(String[] roles, HttpServletRequest request);

}

/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.security;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring security provider implementation.
 *
 * @author Jeroen van Schagen
 * @since Sep 8, 2015
 */
public class SpringSecurityProvider implements SecurityProvider {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthorized(String[] roles) {
        boolean authorized = false;
        if (roles.length == 0) {
            authorized = true;
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                authorized = containsAnyOfOurRoles(authentication, roles);
            }
        }
        return authorized;
    }
    
    private boolean containsAnyOfOurRoles(Authentication authentication, String[] roles) {
        Set<String> authorities = getAuthorities(authentication);
        for (String role : roles) {
            if (authorities.contains(role)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> getAuthorities(Authentication authentication) {
        Set<String> authorities = new HashSet<String>();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            authorities.add(authority.getAuthority());
        }
        return authorities;
    }
    
}

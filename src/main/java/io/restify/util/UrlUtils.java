/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

/**
 * URL parsing functionalities.
 *
 * @author Jeroen van Schagen
 * @since Aug 26, 2015
 */
public class UrlUtils {
    
    public static final String SLASH = "/";

    public static String getPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return request.getRequestURI().substring(contextPath.length());
    }
    
    public static String getRootPath(HttpServletRequest request) {
        String path = getPath(request);
        return StringUtils.substringBetween(path + SLASH, SLASH, SLASH);
    }
    
}

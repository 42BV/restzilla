/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.web.util;

import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

/**
 * URL parsing functionalities.
 *
 * @author Jeroen van Schagen
 * @since Aug 26, 2015
 */
public class UrlUtils {
    
    public static final String SLASH = "/";
    
    private static final String VARIABLE_PATTERN = "\\{.*\\}";

    /**
     * Retrieve the path from a request.
     * 
     * @param request the request
     * @return the path
     */
    public static String getPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return stripSlashes(request.getRequestURI().substring(contextPath.length()));
    }

    /**
     * Removes the slashes from a path.
     * 
     * @param path the raw path
     * @return the same path without slashes
     */
    public static String stripSlashes(String path) {
        if (StringUtils.isBlank(path) || Objects.equals(SLASH, path)) {
            return "";
        }

        if (!path.startsWith(SLASH)) {
            path = SLASH + path;
        }
        if (!path.endsWith(SLASH)) {
            path = path + SLASH;
        }
        return path.substring(1, path.length() - 1);
    }
    
    /**
     * Retrieve the first path element in our request path.
     * 
     * @param request the request
     * @return the first path element
     */
    public static String getBasePath(HttpServletRequest request) {
        String path = getPath(request);
        return StringUtils.substringBefore(path, SLASH);
    }
    
    /**
     * Determine if two paths are similar.
     * 
     * @param leftPath the left path
     * @param rightPath the right path
     * @return {@code true} if similar, else {@code false}
     */
    public static boolean isSamePath(String leftPath, String rightPath) {
        final String[] left = leftPath.split(SLASH);
        final String[] right = rightPath.split(SLASH);
        
        if (left.length == right.length) {
            for (int index = 0; index < left.length; index++) {
                String a = left[index];
                String b = right[index];
                if (a.equals(b) || (a.matches(VARIABLE_PATTERN) && b.matches(VARIABLE_PATTERN))) {
                    return true;
                }
            }
        }
        
        return false;
    }

}

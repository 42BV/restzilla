/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.util;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import io.restify.SortingDefault;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Resolves {@link Pageable} instances from HTTP requests.
 *
 * @author Jeroen van Schagen
 * @since Sep 3, 2015
 */
public class PageableResolver {
    
    public static final String PAGE_PARAMETER = "page";
    public static final String SIZE_PARAMETER = "size";
    public static final String SORT_PARAMETER = "sort";
    public static final String SORT_DELIMITER = ",";
    
    private static final Sort FALLBACK_DEFAULT_SORT = new Sort(Direction.ASC, "id");
    private static final int FALLBACK_DEFAULT_SIZE = 10;

    /**
     * Determine if this request has pagination information.
     * 
     * @param request the request
     * @return {@code true} if pagination information is found, else {@code false}
     */
    public static boolean isSupported(HttpServletRequest request) {
        return isNotBlank(request.getParameter(PAGE_PARAMETER));
    }
    
    /**
     * Extract the {@link Pageable} instance from this request.
     * 
     * @param request the request
     * @param entityClass the entity class, used for defaults
     * @return the resolved pageable
     */
    public static Pageable getPageable(HttpServletRequest request, Class<?> entityClass) {
        int page = Integer.parseInt(request.getParameter(PAGE_PARAMETER));
        int size = getSize(request);
        Sort sort = getSort(request, entityClass);
        return new PageRequest(page, size, sort);
    }

    private static int getSize(HttpServletRequest request) {
        String size = request.getParameter(SIZE_PARAMETER);
        if (isNotBlank(size)) {
            return Integer.parseInt(size);
        } else {
            return FALLBACK_DEFAULT_SIZE;
        }
    }
    
    /**
     * Extract the {@link Sort} instance from this request.
     * 
     * @param request the request
     * @param entityClass the entity class, used for defaults
     * @return the resolved pageable
     */
    public static Sort getSort(HttpServletRequest request, Class<?> entityClass) {
        String sort = request.getParameter(SORT_PARAMETER);
        if (StringUtils.isNotBlank(sort)) {
            Direction direction = Direction.ASC;
            
            String[] properties = sort.split(SORT_DELIMITER);
            if (properties.length > 1) {
                direction = Direction.fromStringOrNull(properties[properties.length - 1]);
                properties = ArrayUtils.remove(properties, properties.length - 1);
            }
            
            return new Sort(direction, properties);
        } else {
            SortingDefault defaults = AnnotationUtils.findAnnotation(entityClass, SortingDefault.class);
            return defaults != null ? new Sort(defaults.direction(), defaults.value()) : FALLBACK_DEFAULT_SORT;
        }
    }

}

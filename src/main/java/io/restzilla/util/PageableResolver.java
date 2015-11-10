/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.util;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import io.restzilla.SortingDefault;
import io.restzilla.SortingDefault.SortingDefaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

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
    
    private static final int FALLBACK_DEFAULT_PAGE = 0;
    private static final int FALLBACK_DEFAULT_SIZE = 10;
    private static final Sort FALLBACK_DEFAULT_SORT = new Sort(Direction.ASC, "id");

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
        int page = getParameterAsInteger(request, PAGE_PARAMETER, FALLBACK_DEFAULT_PAGE);
        int size = getParameterAsInteger(request, SIZE_PARAMETER, FALLBACK_DEFAULT_SIZE);
        Sort sort = getSort(request, entityClass);
        return new PageRequest(page, size, sort);
    }

    private static int getParameterAsInteger(HttpServletRequest request, String name, int defaultValue) {
        String size = request.getParameter(name);
        if (isNotBlank(size)) {
            return Integer.parseInt(size);
        } else {
            return defaultValue;
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
            return getDefaultSort(entityClass);
        }
    }

    private static Sort getDefaultSort(Class<?> entityClass) {
        List<SortingDefault> defaults = findSortAnnotations(entityClass);
        if (defaults.isEmpty()) {
            return FALLBACK_DEFAULT_SORT;
        } else {
            List<Order> orders = new ArrayList<Order>();
            for (SortingDefault fragment : defaults) {
                for (String property : fragment.value()) {
                    orders.add(new Order(fragment.direction(), property));
                }
            }
            return new Sort(orders);
        }
    }
    
    private static List<SortingDefault> findSortAnnotations(Class<?> entityClass) {
        List<SortingDefault> result = new ArrayList<SortingDefault>();
        SortingDefaults multiple = AnnotationUtils.findAnnotation(entityClass, SortingDefaults.class);
        if (multiple != null) {
            result.addAll(Arrays.asList(multiple.value()));
        } else {
            SortingDefault single = AnnotationUtils.findAnnotation(entityClass, SortingDefault.class);
            if (single != null) {
                result.add(single);
            }
        }
        return result;
    }

}

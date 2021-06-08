/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.web.util;

import nl._42.restzilla.RestProperties;
import nl._42.restzilla.SortingDefault;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
    public static Pageable getPageable(HttpServletRequest request, Class<?> entityClass, RestProperties properties) {
        Sort sort = getSort(request, entityClass, properties);
        return getPageable(request, sort, properties);
    }

    /**
     * Extract the {@link Pageable} instance from this request.
     * 
     * @param request the request
     * @param sort the sort to use
     * @return the resolved pageable
     */
    public static Pageable getPageable(HttpServletRequest request, Sort sort, RestProperties properties) {
        int parsed = getParameterAsInteger(request, PAGE_PARAMETER, properties.getDefaultPage());
        int page = Math.max(properties.isOneIndexedParameters() ? parsed - 1 : parsed, 0);
        int size = Math.min(getParameterAsInteger(request, SIZE_PARAMETER, properties.getDefaultSize()), properties.getMaxPageSize());
        return PageRequest.of(page, size, sort);
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
    public static Sort getSort(HttpServletRequest request, Class<?> entityClass, RestProperties properties) {
        String[] sorts = request.getParameterValues(SORT_PARAMETER);
        if (sorts == null) {
            return getDefaultSort(entityClass, properties);
        }
        
        Sort result = parseSort(sorts[0]);
        for (int index = 1; index < sorts.length; index++) {
            result = result.and(parseSort(sorts[index]));
        }
        return result;
    }

    private static Sort parseSort(String sort) {
        Direction direction = Direction.ASC;
        
        String[] properties = sort.split(SORT_DELIMITER);
        if (properties.length > 1) {
            direction = Direction.fromString(properties[properties.length - 1]);
            properties = ArrayUtils.remove(properties, properties.length - 1);
        }
        
        return Sort.by(direction, properties);
    }

    private static Sort getDefaultSort(Class<?> entityClass, RestProperties properties) {
        List<SortingDefault> defaults = findSortAnnotations(entityClass);
        if (defaults.isEmpty()) {
            return properties.getDefaultSort();
        } else {
            List<Order> orders = new ArrayList<Order>();
            for (SortingDefault fragment : defaults) {
                for (String property : fragment.value()) {
                    orders.add(new Order(fragment.direction(), property));
                }
            }
            return Sort.by(orders);
        }
    }
    
    private static List<SortingDefault> findSortAnnotations(Class<?> entityClass) {
        List<SortingDefault> result = new ArrayList<SortingDefault>();
        SortingDefault.SortingDefaults multiple = AnnotationUtils.findAnnotation(entityClass, SortingDefault.SortingDefaults.class);
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

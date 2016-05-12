/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla;

import io.restzilla.util.UrlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Persistable;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;

/**
 * Shows all information of an entity.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class RestInformation {

    private final Class<? extends Persistable<?>> entityClass;
    
    private final Class<?> identifierClass;
    
    private final RestResource entityAnnotation;
    
    private final String basePath;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public RestInformation(Class<?> entityClass) {
        RestResource entityAnnotation = findAnnotation(entityClass);
        Preconditions.checkNotNull(entityAnnotation, "Missing @RestResource annotation for: " + entityClass.getName());
        this.entityAnnotation = entityAnnotation;

        if (!entityAnnotation.value().equals(Object.class)) {
            entityClass = entityAnnotation.value();
        }
        this.entityClass = (Class) entityClass;

        try {
            this.identifierClass = entityClass.getMethod("getId").getReturnType();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not find getId annotation, please implement Persistable", e);
        }
        
        String basePath = entityAnnotation.basePath();
        if (StringUtils.isBlank(basePath)) {
            basePath = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, entityClass.getSimpleName());
        }
        this.basePath = UrlUtils.stripSlashes(basePath);
    }

    private static RestResource findAnnotation(Class<?> entityClass) {
        return entityClass.getAnnotationsByType(RestResource.class)[0];
    }
    
    public static boolean isSupported(Class<?> entityClass) {
        return findAnnotation(entityClass) != null;
    }

    /**
     * Retrieve the base path.
     * 
     * @return the base path
     */
    public String getBasePath() {
        return basePath;
    }
    
    /**
     * Retrieve if read only.
     * 
     * @return the read only
     */
    public boolean isReadOnly() {
        return entityAnnotation.readOnly();
    }
    
    /**
     * Retrieve if paged only.
     * 
     * @return the paged only
     */
    public boolean isPagedOnly() {
        return entityAnnotation.pagedOnly();
    }
    
    /**
     * Retrieve if patch.
     * 
     * @return the patch
     */
    public boolean isPatch() {
        return entityAnnotation.patch();
    }
    
    //
    // Typing
    //

    /**
     * Retrieve the entity class.
     * 
     * @return the entity class
     */
    public Class<? extends Persistable<?>> getEntityClass() {
        return entityClass;
    }
    
    /**
     * Retrieve the identifier class.
     * 
     * @return the identifier class
     */
    public Class<?> getIdentifierClass() {
        return identifierClass;
    }

    /**
     * Determine the input type.
     * 
     * @param config the configuration
     * @return the input type
     */
    public Class<?> getInputType(RestConfig config) {
        Class<?> inputType = entityClass;
        if (isCustom(config.inputType())) {
            inputType = config.inputType();
        } else if (isCustom(entityAnnotation.inputType())) {
            inputType = entityAnnotation.inputType();
        }
        return inputType;
    }

    /**
     * Determine the result type.
     * 
     * @param config the configuration
     * @return the result type
     */
    public Class<?> getResultType(RestConfig config) {
        return getResultInfo(config).getType();
    }

    /**
     * Determine the result type.
     * 
     * @param config the configuration
     * @return the result type
     */
    public ResultInformation getResultInfo(RestConfig config) {
        if (isCustom(config.resultType())) {
            return new ResultInformation(config.resultType(), config.resultByQuery());
        } else {
            return getResultInfo();
        }
    }
    
    private ResultInformation getResultInfo() {
        if (isCustom(entityAnnotation.resultType())) {
            return new ResultInformation(entityAnnotation.resultType(), entityAnnotation.resultByQuery());
        } else {
            return new ResultInformation(entityClass, false);
        } 
    }

    private static boolean isCustom(Class<?> clazz) {
        return !Object.class.equals(clazz);
    }
    
    //
    // Queries
    //

    public List<QueryInformation> getQueries() {
        List<QueryInformation> queries = new ArrayList<QueryInformation>();
        for (RestQuery annotation : entityAnnotation.queries()) {
            queries.add(new QueryInformation(annotation));
        }
        return queries;
    }

    public QueryInformation findQuery(Map<String, String[]> requestParameters) {
        for (RestQuery query : entityAnnotation.queries()) {
            if (isMatchingParameters(query.parameters(), requestParameters)) {
                return new QueryInformation(query);
            }
        }
        return null;
    }
    
    private boolean isMatchingParameters(String[] parameters, Map<String, String[]> requestParameters) {
        for (String parameter : parameters) {
            if (parameter.contains("=")) {
                String name = StringUtils.substringBefore(parameter, "=");
                String expected = StringUtils.substringAfter(parameter, "=");
                String value = getSingleParameterValue(name, requestParameters);
                if (!expected.equals(value)) {
                    return false;
                }
            } else {
                String value = getSingleParameterValue(parameter, requestParameters);
                if (StringUtils.isBlank(value)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private String getSingleParameterValue(String parameterName, Map<String, String[]> requestParameters) {
        String[] values = requestParameters.get(parameterName);
        if (values == null || values.length == 0) {
            return "";
        } else {
            return StringUtils.defaultString(values[0], "");
        }
    }
    
    //
    // Security
    //
    
    /**
     * Retrieve the security expressions to read data.
     * 
     * @return the read expressions
     */
    public String[] getReadSecured() {
        return entityAnnotation.reader();
    }
    
    /**
     * Retrieve the security expressions to modify data.
     * 
     * @return the modify expressions
     */
    public String[] getModifySecured() {
        return entityAnnotation.modifier();
    }
    
    //
    // Custom configuration
    //

    /**
     * Retrieve the {@code findAll} configuration.
     * 
     * @return the configuration
     */
    public RestConfig findAll() {
        return entityAnnotation.findAll();
    }
    
    /**
     * Retrieve the {@code findOne} configuration.
     * 
     * @return the configuration
     */
    public RestConfig findOne() {
        return entityAnnotation.findOne();
    }
    
    /**
     * Retrieve the {@code create} configuration.
     * 
     * @return the configuration
     */
    public RestConfig create() {
        return entityAnnotation.create();
    }
    
    /**
     * Retrieve the {@code update} configuration.
     * 
     * @return the configuration
     */
    public RestConfig update() {
        return entityAnnotation.update();
    }
    
    /**
     * Retrieve the {@code delete} configuration.
     * 
     * @return the configuration
     */
    public RestConfig delete() {
        return entityAnnotation.delete();
    }
    
    /**
     * Information about the REST result.
     *
     * @author Jeroen van Schagen
     * @since Dec 10, 2015
     */
    public static class ResultInformation {
        
        private final Class<?> type;
        
        private final boolean byQuery;
        
        private ResultInformation(Class<?> type, boolean byQuery) {
            this.type = type;
            this.byQuery = byQuery;
        }
        
        public Class<?> getType() {
            return type;
        }
        
        public boolean isByQuery() {
            return byQuery;
        }
        
    }
    
    /**
     * Information about the REST query.
     *
     * @author Jeroen van Schagen
     * @since Dec 10, 2015
     */
    public class QueryInformation {
        
        private final RestQuery queryAnnotation;

        public QueryInformation(RestQuery queryAnnotation) {
            this.queryAnnotation = queryAnnotation;
        }
        
        public String getMethodName() {
            return queryAnnotation.method();
        }
        
        public List<String> getRawParameters() {
            return Arrays.asList(queryAnnotation.parameters());
        }

        /**
         * Retrieve the variable parameter names.
         * 
         * @return the parameter names
         */
        public List<String> getParameterNames() {
            List<String> parameterNames = new ArrayList<String>();
            for (String parameter : queryAnnotation.parameters()) {
                if (!parameter.contains("=")) {
                    parameterNames.add(parameter);
                }
            }
            return parameterNames;
        }
        
        /**
         * Determine the entity type.
         * 
         * @return the entity type
         */
        public Class<?> getEntityType() {
            if (isCustom(queryAnnotation.entityType())) {
                return queryAnnotation.entityType();
            } else if (entityAnnotation.resultByQuery()) {
                return entityAnnotation.resultType();
            } else {
                return entityClass;
            }
        }

        /**
         * Determine the result type.
         * 
         * @return the result type
         */
        public Class<?> getResultType() {
            if (isCustom(queryAnnotation.resultType())) {
                return queryAnnotation.resultType();
            }
            return getEntityType();
        }

        /**
         * Determine if the finder results in a single result.
         * 
         * @return {@code true} when unique
         */
        public boolean isSingleResult() {
            return queryAnnotation.unique();
        }
        
        /**
         * Retrieve the security rules for this particular finder.
         * 
         * @return the security rules
         */
        public String[] getSecured() {
            return queryAnnotation.secured();
        }

    }

}

/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.web;

import io.restzilla.RestConfig;
import io.restzilla.RestQuery;
import io.restzilla.RestResource;
import io.restzilla.util.UrlUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Persistable;
import org.springframework.web.bind.annotation.RequestMapping;

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
    
    private final RestResource annotation;
    
    private final ResultInformation resultInfo;

    private final String basePath;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public RestInformation(Class<?> baseClass) {
        RestResource annotation = findAnnotation(baseClass, RestResource.class);
        Preconditions.checkNotNull(annotation, "Missing @RestResource annotation for: " + baseClass.getName());
        this.annotation = annotation;

        this.entityClass = (Class) getFirstCustom(annotation.entityType(), baseClass);
        try {
            this.identifierClass = entityClass.getMethod("getId").getReturnType();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not find getId annotation, please implement Persistable", e);
        }
        
        this.resultInfo = resolveResultInfo();
        this.basePath = resolveBasePath(baseClass);
    }

    private String resolveBasePath(Class<?> baseClass) {
        // By default use the configured base path
        String basePath = annotation.basePath();
        if (StringUtils.isBlank(basePath)) {
            // Use controller mappings whenever possible
            RequestMapping mapping = findAnnotation(baseClass, RequestMapping.class);
            if (mapping != null && mapping.value().length > 0) {
                basePath = mapping.value()[0];
            }
            // Generate base path based on class name
            if (StringUtils.isBlank(basePath)) {
                basePath = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, entityClass.getSimpleName());
            }
        }
        return UrlUtils.stripSlashes(basePath);
    }
    
    private static <T extends Annotation> T findAnnotation(Class<?> containerClass, Class<T> annotationType) {
        T[] annotations = containerClass.getAnnotationsByType(annotationType);
        return annotations.length > 0 ? annotations[0] : null;
    }
    
    public static boolean isSupported(Class<?> entityClass) {
        return findAnnotation(entityClass, RestResource.class) != null;
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
        return annotation.readOnly();
    }
    
    /**
     * Retrieve if paged only.
     * 
     * @return the paged only
     */
    public boolean isPagedOnly() {
        return annotation.pagedOnly();
    }
    
    /**
     * Retrieve if patch.
     * 
     * @return the patch
     */
    public boolean isPatch() {
        return annotation.patch();
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
        } else if (isCustom(annotation.inputType())) {
            inputType = annotation.inputType();
        }
        return inputType;
    }

    /**
     * Determines if another result information has a custom query.
     * 
     * @param resultInfo the other result information
     * @return {@code true} when custom query, else {@code false}
     */
    public boolean hasCustomQuery(ResultInformation resultInfo) {
        return !entityClass.equals(resultInfo.getQueryType());
    }
    
    /**
     * Determine the result information.
     * 
     * @return the result information
     */
    public ResultInformation getResultInfo() {
        return resultInfo;
    }

    /**
     * Determine the result type.
     * 
     * @param config the configuration
     * @return the result type
     */
    public Class<?> getResultType(RestConfig config) {
        return getResultInfo(config).getResultType();
    }
    
    /**
     * Determine the result information.
     * 
     * @param config the configuration
     * @return the result information
     */
    public ResultInformation getResultInfo(RestConfig config) {
        return resolveResultInfo(config.queryType(), config.resultType(), resultInfo);
    }
    
    /**
     * Determine the result information.
     * @param configuredQueryType the query type
     * @param configuredResultType the result type
     * @param base the parent result information
     * @return the configured result information
     */
    private ResultInformation resolveResultInfo(Class<?> configuredQueryType, Class<?> configuredResultType, ResultInformation base) {
        Class<?> queryType = getFirstCustom(configuredQueryType, base.getQueryType());
        Class<?> baseResultType = queryType;
        if (!base.getResultType().equals(base.getQueryType())) {
            baseResultType = base.getResultType();
        }
        Class<?> resultType = getFirstCustom(configuredResultType, baseResultType);
        return new ResultInformation(queryType, resultType);
    }
    
    private ResultInformation resolveResultInfo() {
        Class<?> queryType = getFirstCustom(annotation.queryType(), entityClass);
        Class<?> resultType = getFirstCustom(annotation.resultType(), queryType);
        return new ResultInformation(queryType, resultType);
    }

    private static Class<?> getFirstCustom(Class<?>... classes) {
        Class<?> result = null;
        for (Class<?> clazz : classes) {
            if (isCustom(clazz)) {
                result = clazz;
                break;
            }
        }
        return result;
    }

    private static boolean isCustom(Class<?> clazz) {
        return !Object.class.equals(clazz);
    }
    
    //
    // Queries
    //

    public List<QueryInformation> getQueries() {
        List<QueryInformation> queries = new ArrayList<QueryInformation>();
        for (RestQuery annotation : annotation.queries()) {
            queries.add(new QueryInformation(annotation));
        }
        return queries;
    }

    public QueryInformation findQuery(Map<String, String[]> requestParameters) {
        for (RestQuery query : annotation.queries()) {
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
        return annotation.readSecured();
    }
    
    /**
     * Retrieve the security expressions to modify data.
     * 
     * @return the modify expressions
     */
    public String[] getModifySecured() {
        return annotation.modifySecured();
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
        return annotation.findAll();
    }
    
    /**
     * Retrieve the {@code findOne} configuration.
     * 
     * @return the configuration
     */
    public RestConfig findOne() {
        return annotation.findOne();
    }
    
    /**
     * Retrieve the {@code create} configuration.
     * 
     * @return the configuration
     */
    public RestConfig create() {
        return annotation.create();
    }
    
    /**
     * Retrieve the {@code update} configuration.
     * 
     * @return the configuration
     */
    public RestConfig update() {
        return annotation.update();
    }
    
    /**
     * Retrieve the {@code delete} configuration.
     * 
     * @return the configuration
     */
    public RestConfig delete() {
        return annotation.delete();
    }
    
    /**
     * Information about the REST result.
     *
     * @author Jeroen van Schagen
     * @since Dec 10, 2015
     */
    public static class ResultInformation {
        
        private final Class<?> queryType;
        
        private final Class<?> resultType;
        
        private ResultInformation(Class<?> queryType, Class<?> resultType) {
            this.queryType = queryType;
            this.resultType = resultType;
        }
        
        public Class<?> getQueryType() {
            return queryType;
        }
        
        public Class<?> getResultType() {
            return resultType;
        }
        
    }
    
    /**
     * Information about the REST query.
     *
     * @author Jeroen van Schagen
     * @since Dec 10, 2015
     */
    public class QueryInformation {
        
        private final RestQuery query;

        public QueryInformation(RestQuery query) {
            this.query = query;
        }
        
        /**
         * Retrieves the method name.
         * @return the method name
         */
        public String getMethodName() {
            return query.method();
        }
        
        /**
         * Retrieves the raw parameters.
         * @return the parameters
         */
        public List<String> getRawParameters() {
            return Arrays.asList(query.parameters());
        }

        /**
         * Retrieve the variable parameter names.
         * @return the parameter names
         */
        public List<String> getParameterNames() {
            List<String> parameterNames = new ArrayList<String>();
            for (String parameter : query.parameters()) {
                if (!parameter.contains("=")) {
                    parameterNames.add(parameter);
                }
            }
            return parameterNames;
        }
        
        /**
         * Retrieves the result information.
         * @return the result information
         */
        public ResultInformation getResultInfo() {
            return resolveResultInfo(query.queryType(), query.resultType(), resultInfo);
        }

        /**
         * Determine if the finder results in a single result.
         * @return {@code true} when unique
         */
        public boolean isSingleResult() {
            return query.unique();
        }
        
        /**
         * Retrieve the security rules for this particular finder.
         * @return the security rules
         */
        public String[] getSecured() {
            return query.secured();
        }

    }

}

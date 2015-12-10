/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Persistable;

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
    
    private final String basePath;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public RestInformation(Class<?> entityClass, String basePath, RestResource annotation) throws NoSuchMethodException {
        if (!(Persistable.class.isAssignableFrom(entityClass))) {
            throw new IllegalStateException("Entity does not extend from Persistable");
        }
        this.entityClass = (Class) entityClass;
        this.identifierClass = entityClass.getMethod("getId").getReturnType();
        this.basePath = basePath;
        this.annotation = annotation;
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
        return isCustom(config.inputType()) ? config.inputType() : entityClass;
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
        if (isCustom(annotation.resultType())) {
            return new ResultInformation(annotation.resultType(), annotation.resultByQuery());
        } else {
            return new ResultInformation(entityClass, false);
        } 
    }

    private static boolean isCustom(Class<?> clazz) {
        return !Object.class.equals(clazz);
    }
    
    public QueryInformation findCustomQuery(Map<String, String[]> requestParameters) {
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
        
        private final RestQuery annotation;

        public QueryInformation(RestQuery annotation) {
            this.annotation = annotation;
        }
        
        public String getMethodName() {
            return annotation.method();
        }

        public List<String> getParameterNames() {
            List<String> parameterNames = new ArrayList<String>();
            for (String parameter : annotation.parameters()) {
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
            return isCustom(annotation.entityType()) ? annotation.entityType() : entityClass;
        }

        /**
         * Determine the result type.
         * 
         * @return the result type
         */
        public Class<?> getResultType() {
            if (isCustom(annotation.resultType())) {
                return annotation.resultType();
            }
            return getEntityType();
        }

        /**
         * Determine if the finder results in a single result.
         * 
         * @return {@code true} when unique
         */
        public boolean isUnique() {
            return annotation.unique();
        }
        
        /**
         * Retrieve the security rules for this particular finder.
         * 
         * @return the security rules
         */
        public String[] getSecured() {
            return annotation.secured();
        }

    }

}

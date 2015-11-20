/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla;

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
        ResultInformation result = getResultInfo(config);
        return result.getType();
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
    
    public static class ResultInformation {
        
        private final Class<?> type;
        
        private final boolean query;

        private ResultInformation(Class<?> type, boolean query) {
            this.type = type;
            this.query = query;
        }
        
        public Class<?> getType() {
            return type;
        }
        
        public boolean isQuery() {
            return query;
        }

    }

}

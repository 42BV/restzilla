/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify;

import static org.apache.commons.lang3.StringUtils.isBlank;

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
    
    private final RestEnable annotation;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public RestInformation(Class<?> entityClass, RestEnable annotation) throws NoSuchMethodException {
        if (!(Persistable.class.isAssignableFrom(entityClass))) {
            throw new IllegalStateException("Entity does not extend from Persistable");
        }
        this.entityClass = (Class) entityClass;
        this.identifierClass = entityClass.getMethod("getId").getReturnType();
        this.annotation = annotation;
    }

    /**
     * Retrieve the base path.
     * 
     * @return the base path
     */
    public String getBasePath() {
        String basePath = annotation.basePath();
        if (isBlank(basePath)) {
            basePath = entityClass.getSimpleName().toLowerCase();
        }
        return basePath;
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
        return isCustom(config.resultType()) ? config.resultType() : getResultType();
    }
    
    private Class<?> getResultType() {
        return isCustom(annotation.resultType()) ? annotation.resultType() : entityClass;
    }

    private static boolean isCustom(Class<?> clazz) {
        return !Object.class.equals(clazz);
    }
    
    // Specific configurations
    
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

}

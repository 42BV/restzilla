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
public class EntityInformation {

    private final Class<?> entityClass;
    
    private final Class<?> identifierClass;
    
    private final EnableRest annotation;
    
    public EntityInformation(Class<?> entityClass, EnableRest annotation) throws NoSuchMethodException {
        this.entityClass = entityClass;
        if (!(Persistable.class.isAssignableFrom(entityClass))) {
            throw new IllegalStateException("Entity does not extend from Persistable");
        }
        this.identifierClass = entityClass.getMethod("getId").getReturnType();
        this.annotation = annotation;
    }

    public String getBasePath() {
        String basePath = annotation.basePath();
        if (isBlank(basePath)) {
            basePath = entityClass.getSimpleName().toLowerCase();
        }
        return basePath;
    }
    
    public Class<?> getEntityClass() {
        return entityClass;
    }
    
    public Class<?> getIdentifierClass() {
        return identifierClass;
    }
    
    public Class<?> getResultType() {
        return isCustom(annotation.resultType()) ? annotation.resultType() : entityClass;
    }
    
    public Class<?> getCreateType() {
        return isCustom(annotation.createType()) ? annotation.createType() : entityClass;
    }
    
    public Class<?> getUpdateType() {
        return isCustom(annotation.updateType()) ? annotation.updateType() : entityClass;
    }
    
    private static boolean isCustom(Class<?> clazz) {
        return !Object.class.equals(clazz);
    }

    public boolean isReadonly() {
        return annotation.readOnly();
    }

}

/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.rest;

import static org.apache.commons.lang3.StringUtils.isBlank;

import org.springframework.data.domain.Persistable;

/**
 * Shows all information of an entity.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class EntityInformation {
    
    private final String basePath;
    
    private final Class<?> entityClass;
    
    private final Class<?> identifierClass;
    
    private final Class<?> resultType;
    
    private final Class<?> createType;
    
    private final Class<?> updateType;
    
    public EntityInformation(Class<?> entityClass, EnableRest annotation) throws NoSuchMethodException {
        String basePath = annotation.basePath();
        if (isBlank(basePath)) {
            basePath = entityClass.getSimpleName().toLowerCase();
        }
        this.basePath = basePath;

        this.entityClass = entityClass;
        if (!(Persistable.class.isAssignableFrom(entityClass))) {
            throw new IllegalStateException("Entity does not extend from Persistable");
        }
        this.identifierClass = entityClass.getMethod("getId").getReturnType();

        this.resultType = isCustom(annotation.resultType()) ? annotation.resultType() : entityClass;
        this.createType = isCustom(annotation.createType()) ? annotation.createType() : entityClass;
        this.updateType = isCustom(annotation.updateType()) ? annotation.updateType() : entityClass;
    }

    private boolean isCustom(Class<?> clazz) {
        return !Object.class.equals(clazz);
    }

    public String getBasePath() {
        return basePath;
    }
    
    public Class<?> getEntityClass() {
        return entityClass;
    }
    
    public Class<?> getIdentifierClass() {
        return identifierClass;
    }
    
    public Class<?> getResultType() {
        return resultType;
    }
    
    public Class<?> getCreateType() {
        return createType;
    }
    
    public Class<?> getUpdateType() {
        return updateType;
    }

}

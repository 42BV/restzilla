/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.builder;

import nl._42.restzilla.model.OtherEntity;
import nl._42.restzilla.model.WithOtherEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 *
 * @author jeroen
 * @since Aug 24, 2015
 */
@Component
@Transactional
public class OtherBuilder {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public WithOtherEntity createOther(String name) {
        OtherEntity other = new OtherEntity();
        other.setName(name);
        entityManager.persist(other);
        
        WithOtherEntity entity = new WithOtherEntity();
        entity.setOtherName(name);
        entityManager.persist(entity);
        
        new DirectFieldAccessor(entity).setPropertyValue("id", other.getId());
        entityManager.merge(entity);
        
        return entity;
    }

}

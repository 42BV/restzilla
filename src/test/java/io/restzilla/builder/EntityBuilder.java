/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.builder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;

@Component
public class EntityBuilder {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public void save(Object entity) {
        entityManager.persist(entity);
    }

}

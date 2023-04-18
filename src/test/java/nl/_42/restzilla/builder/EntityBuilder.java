/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.builder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EntityBuilder {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Transactional
    public void save(Object entity) {
        entityManager.persist(entity);
    }

    public <T> T get(Class<T> entityClass, Long id) {
        return entityManager.find(entityClass, id);
    }

}

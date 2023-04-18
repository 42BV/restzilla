/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.builder;

import nl._42.restzilla.model.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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
public class UserBuilder {
    
    @PersistenceContext
    private EntityManager entityManager;

    public User createUser(String name) {
        User user = new User();
        user.setName(name);
        entityManager.persist(user);
        return user;
    }

}

/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.rest.repository;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

/**
 * Builds default repositories.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class SpringDataJpaRepositoryFactory<T, ID extends Serializable> {

    private final AutowireCapableBeanFactory beanFactory;
    
    private final Class<T> entityClass;

    @PersistenceContext
    private EntityManager entityManager;

    public SpringDataJpaRepositoryFactory(AutowireCapableBeanFactory beanFactory, Class<T> entityClass) {
        this.beanFactory = beanFactory;
        this.entityClass = entityClass;
        beanFactory.autowireBean(this);
    }
    
    public SimpleJpaRepository<T, ID> build() {
        SimpleJpaRepository<T, ID> delegate = new SimpleJpaRepository<T, ID>(entityClass, entityManager);
        beanFactory.autowireBean(delegate);
        return delegate;
    }

}

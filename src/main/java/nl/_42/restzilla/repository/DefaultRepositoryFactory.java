/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * Responsible for generating new CRUD service and repository instances.
 *
 * @author Jeroen van Schagen
 * @since Sep 18, 2015
 */
public class DefaultRepositoryFactory implements CrudRepositoryFactory {

    private final ConfigurableListableBeanFactory beanFactory;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Create a new {@link DefaultRepositoryFactory} instance.
     *
     * @param applicationContext the application context for autowiring
     */
    public DefaultRepositoryFactory(final ApplicationContext applicationContext) {
        beanFactory = (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        beanFactory.autowireBean(this);
    }

    @Override
    public<T extends Persistable<ID>, ID extends Serializable> JpaRepository<T, ID> build(Class<T> entityClass) {

        JpaRepository<T, ?> repository = new SimpleJpaRepository<>(entityClass, entityManager);
        beanFactory.autowireBean(repository);

        final String beanName = nameOf(entityClass);
        Object proxy = beanFactory.applyBeanPostProcessorsAfterInitialization(repository, beanName);
        return (JpaRepository<T, ID>) proxy;
    }

    private static String nameOf(Class<?> entityClass) {
        return StringUtils.uncapitalize(entityClass.getSimpleName()) + "Repository";
    }

}

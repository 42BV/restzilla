/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.repository;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
    public<T extends Persistable<ID>, ID extends Serializable> PagingAndSortingRepository<T, ID> build(
      final Class<T> entityClass
    ) {

        PagingAndSortingRepository<T, ?> repository = new SimpleJpaRepository<T, ID>(entityClass, entityManager);
        beanFactory.autowireBean(repository);

        final String beanName = nameOf(entityClass);
        Object proxy = beanFactory.applyBeanPostProcessorsAfterInitialization(repository, beanName);
        return (PagingAndSortingRepository<T, ID>) proxy;
    }

    private static String nameOf(Class<?> entityClass) {
        return StringUtils.uncapitalize(entityClass.getSimpleName()) + "Repository";
    }

}

/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.registry;

import io.restzilla.service.CrudService;
import io.restzilla.service.DefaultCrudService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
class CrudServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCrudService.class);

    /**
     * Bean factory used to autowire and register generated beans.
     */
    private final ConfigurableListableBeanFactory beanFactory;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Create a new {@link CrudServiceFactory} instance.
     *
     * @param applicationContext the application context for autowiring
     */
    CrudServiceFactory(ApplicationContext applicationContext) {
        beanFactory = (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        beanFactory.autowireBean(this);
    }

    <T extends Persistable<ID>, ID extends Serializable> PagingAndSortingRepository<T, ID> buildRepository(Class<T> entityClass) {
        SimpleJpaRepository<T, ?> repository = new SimpleJpaRepository<T, ID>(entityClass, entityManager);
        beanFactory.autowireBean(repository);

        final String beanName = StringUtils.uncapitalize(entityClass.getSimpleName()) + "Repository";
        Object proxy = beanFactory.applyBeanPostProcessorsAfterInitialization(repository, beanName);
        registerSafely(generateName(beanName), proxy);
        return (PagingAndSortingRepository<T, ID>) proxy;
    }

    private void registerSafely(String beanName, Object bean) {
        try {
            beanFactory.registerSingleton(beanName, bean);
        } catch (RuntimeException rte) {
            LOGGER.warn("Could not dynamically register CRUD bean: " + beanName, rte);
        }
    }

    <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> buildService(Class<T> entityClass, PagingAndSortingRepository<T, ID> repository) {
        DefaultCrudService<T, ID> service = new DefaultCrudService<T, ID>(entityClass, repository);
        beanFactory.autowireBean(service);

        final String beanName = StringUtils.uncapitalize(entityClass.getSimpleName()) + "Service";
        Object proxy = beanFactory.applyBeanPostProcessorsAfterInitialization(service, beanName);
        registerSafely(generateName(beanName), proxy);

        return (CrudService<T, ID>) proxy;
    }

    private String generateName(String baseName) {
        return baseName + "_" + RandomStringUtils.randomAlphabetic(6);
    }

}

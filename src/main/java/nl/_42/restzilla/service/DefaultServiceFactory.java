/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.service;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * Responsible for generating new CRUD service and repository instances.
 *
 * @author Jeroen van Schagen
 * @since Sep 18, 2015
 */
public class DefaultServiceFactory implements CrudServiceFactory {

    private final ConfigurableListableBeanFactory beanFactory;

    /**
     * Create a new {@link DefaultServiceFactory} instance.
     *
     * @param applicationContext the application context for autowiring
     */
    public DefaultServiceFactory(final ApplicationContext applicationContext) {
        this.beanFactory = (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
    }

    @Override
    public <T extends Persistable<ID>, ID extends Serializable> CrudService<T, ID> build(
      final Class<T> entityClass,
      final JpaRepository<T, ID> repository
    ) {

        CrudService<T, ID> service = new DefaultCrudService<>(entityClass, repository);
        beanFactory.autowireBean(service);

        final String beanName = nameOf(entityClass);
        Object proxy = beanFactory.applyBeanPostProcessorsAfterInitialization(service, beanName);
        return (CrudService<T, ID>) proxy;
    }

    private static String nameOf(Class<?> entityClass) {
        return StringUtils.uncapitalize(entityClass.getSimpleName()) + "Service";
    }

}

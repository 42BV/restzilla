package io.restzilla.config.registry;

import io.restzilla.service.CrudService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;

/**
 * Registry for internal services.
 *
 * @author Jeroen van Schagen
 * @since Dec 10, 2015
 */
class Services {

    private final ApplicationContext applicationContext;
    
    private Map<Class<?>, CrudService<?, ?>> instances;

    public Services(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @SuppressWarnings("rawtypes")
    private void init() {
        if (instances == null) {
            instances = new HashMap<Class<?>, CrudService<?, ?>>();
            Map<String, CrudService> services = applicationContext.getBeansOfType(CrudService.class);
            for (CrudService<?, ?> service : services.values()) {
                instances.put(service.getEntityClass(), service);
            }
        }
    }
    
    /**
     * Retrieves the entity class.
     * 
     * @param entityClass the entity class
     * @return the retrieved entity class
     */
    CrudService<?, ?> getByEntityClass(Class<?> entityClass) {
        init(); // Lazy initialization
        return instances.get(entityClass);
    }

}
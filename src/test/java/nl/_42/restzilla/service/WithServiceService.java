/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.service;

import nl._42.restzilla.DefaultService;
import nl._42.restzilla.model.User;
import nl._42.restzilla.model.WithService;
import nl._42.restzilla.registry.CrudServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@DefaultService
public class WithServiceService extends DefaultCrudService<WithService, Long> {

    private CrudService<User, Long> userService;

    @Override
    public <S extends WithService> S save(S entity) {
        entity.setName(entity.getName() + " with " + userService.getEntityClass().getSimpleName() + "!");
        return super.save(entity);
    }
    
    /**
     * Finder method on service level.
     * @return the users
     */
    @Transactional(readOnly = true)
    public List<WithService> findAllByService() {
        List<WithService> entities = new ArrayList<WithService>();
        WithService entity = new WithService();
        entity.setId(42L);
        entity.setName("Service generated");
        entities.add(entity);
        return entities;
    }

    /**
     * Configure the user service. This is merely a check to ensure
     * that generated services can also be injected.
     * @param services the services registry
     */
    @Lazy
    @Autowired
    public void setServices(CrudServiceRegistry services) {
        this.userService = services.getService(User.class);
    }

}

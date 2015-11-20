/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service;

import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import io.restzilla.model.User;
import io.restzilla.model.WithService;
import io.restzilla.service.impl.DefaultCrudService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Scope(proxyMode = TARGET_CLASS)
public class WithServiceService extends DefaultCrudService<WithService, Long> {

    @SuppressWarnings("unused")
    private CrudService<User, Long> userService;

    @Override
    public <S extends WithService> S save(S entity) {
        entity.setName(entity.getName() + " with " + userService.getEntityClass().getSimpleName() + "!");
        return super.save(entity);
    }
    
    @Lazy
    @Autowired
    @Qualifier("UserService")
    public void setUserService(CrudService<User, Long> userService) {
        this.userService = userService;
    }

}

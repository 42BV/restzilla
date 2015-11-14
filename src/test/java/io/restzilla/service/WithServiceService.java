/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service;

import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import io.restzilla.model.WithService;
import io.restzilla.service.impl.AbstractCrudService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Scope(proxyMode = TARGET_CLASS)
public class WithServiceService extends AbstractCrudService<WithService, Long> {

    @Override
    public <S extends WithService> S save(S entity) {
        entity.setName(entity.getName() + "!");
        return super.save(entity);
    }

}

/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.service;

import io.restify.model.WithService;
import io.restify.repository.WithServiceRepository;
import io.restify.service.AbstractCrudService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WithServiceService extends AbstractCrudService<WithService, Long> {
    
    @Autowired
    public WithServiceService(WithServiceRepository repository) {
        super(repository, WithService.class);
    }
    
    @Override
    public <S extends WithService> S save(S entity) {
        entity.setName(entity.getName() + "!");
        return super.save(entity);
    }

}

/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service;

import io.restzilla.model.WithRollback;
import io.restzilla.service.impl.TransactionalCrudService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WithRollbackService extends TransactionalCrudService<WithRollback, Long> {

    @Override
    public <S extends WithRollback> S save(S entity) {
        throw new UnsupportedOperationException("Unsupported, perform rollback.");
    }

}

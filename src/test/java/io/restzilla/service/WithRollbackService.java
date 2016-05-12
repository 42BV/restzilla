/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service;

import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import io.restzilla.model.WithRollback;
import io.restzilla.service.impl.DefaultCrudService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Scope(proxyMode = TARGET_CLASS)
public class WithRollbackService extends DefaultCrudService<WithRollback, Long> {

    @Override
    public <S extends WithRollback> S save(S entity) {
        throw new UnsupportedOperationException("Unsupported, perform rollback.");
    }

}

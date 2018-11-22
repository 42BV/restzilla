/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.service;

import nl._42.restzilla.model.WithProxyService;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WithProxyServiceService extends DefaultCrudService<WithProxyService, Long> {

    // Should throw an exception because the method is of proxy
    public List<WithProxyService> findAllByAge(int age) {
        return Collections.emptyList();
    }

}

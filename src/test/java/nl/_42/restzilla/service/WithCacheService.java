package nl._42.restzilla.service;

import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import nl._42.restzilla.model.WithCache;
import nl._42.restzilla.repository.WithCacheRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Scope(proxyMode = TARGET_CLASS)
public class WithCacheService extends DefaultCrudService<WithCache, Long> {
    
    @Autowired
    public WithCacheService(WithCacheRepository repository, Cache cache) {
        super(repository);
        setCache(cache);
    }
    
}

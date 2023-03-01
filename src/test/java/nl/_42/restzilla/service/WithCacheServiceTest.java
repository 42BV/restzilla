package nl._42.restzilla.service;

import nl._42.restzilla.AbstractSpringTest;
import nl._42.restzilla.model.WithCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;

import java.util.Optional;

public class WithCacheServiceTest extends AbstractSpringTest {
    
    @Autowired
    private WithCacheService service;

    @Autowired
    private Cache cache;
    
    @BeforeEach
    public void testSetup() {
        cache.clear();
        
        WithCache entity = new WithCache();
        entity.setId(42L);
        entity.setName("Test");
        
        cache.put("find(42)", Optional.of(entity));
    }

    @Test
    public void testCacheFindOne() {
        WithCache entity = service.findOne(42L);
        Assertions.assertEquals("Test", entity.getName());
    }
    
    @Test
    public void testCacheFindOneNotFound() {
        Optional<WithCache> entity = service.find(24L);
        Assertions.assertEquals(false, entity.isPresent());
    }
    
    @Test
    public void testUpdateCache() {
        WithCache entity = new WithCache();
        entity.setName("Other");
        
        WithCache result = service.save(entity);
        Assertions.assertEquals(result.getId(), entity.getId());

        Assertions.assertEquals(1, service.findAll().size());

        WithCache cached1 = service.findOne(result.getId());
        WithCache cached2 = service.findOne(result.getId());
        Assertions.assertEquals(cached1, cached2);
        
        service.delete(cached2);
        Assertions.assertEquals(0, service.findAll().size());
    }

}

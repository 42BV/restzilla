package nl._42.restzilla.service;

import nl._42.restzilla.AbstractSpringTest;
import nl._42.restzilla.model.WithCache;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;

public class WithCacheServiceTest extends AbstractSpringTest {
    
    @Autowired
    private WithCacheService service;

    @Autowired
    private Cache cache;
    
    @Before
    public void testSetup() {
        cache.clear();
        
        WithCache entity = new WithCache();
        entity.setId(42L);
        entity.setName("Test");
        
        cache.put("findAll()", Arrays.asList(entity));
        cache.put("findOne(42)", entity);
    }

    @Test
    public void testCacheFindAll() {
        List<WithCache> entities = service.findAll();
        Assert.assertEquals(1, entities.size());
        Assert.assertEquals("Test", entities.get(0).getName());
    }

    @Test
    public void testCacheFindOne() {
        WithCache entity = service.findOne(42L);
        Assert.assertEquals("Test", entity.getName());
    }
    
    @Test
    public void testCacheFindOneNotFound() {
        Optional<WithCache> entity = service.find(24L);
        Assert.assertEquals(false, entity.isPresent());
    }
    
    @Test
    public void testUpdateCache() {
        WithCache entity = new WithCache();
        entity.setName("Other");
        
        WithCache result = service.save(entity);
        Assert.assertEquals(result.getId(), entity.getId());
        
        Assert.assertEquals(1, service.findAll().size());

        WithCache cached1 = service.findOne(result.getId());
        WithCache cached2 = service.findOne(result.getId());
        Assert.assertEquals(cached1, cached2);
        
        service.delete(cached2);
        Assert.assertEquals(0, service.findAll().size());
    }

}

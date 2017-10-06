package io.restzilla.service;

import io.restzilla.AbstractSpringTest;
import io.restzilla.model.WithCache;

import java.util.Arrays;
import java.util.List;

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
        WithCache entity = service.findOne(24L);
        Assert.assertNull(entity);
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
        
        service.delete(result.getId());
        Assert.assertEquals(0, service.findAll().size());
    }

}

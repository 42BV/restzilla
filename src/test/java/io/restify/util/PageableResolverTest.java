/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.util;

import io.restify.SortingDefault;
import io.restify.model.User;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.mock.web.MockHttpServletRequest;

public class PageableResolverTest {
    
    @Test
    public void testIsSupportedTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Assert.assertFalse(PageableResolver.isSupported(request));
    }
    
    @Test
    public void testIsSupportedFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(PageableResolver.PAGE_PARAMETER, "1");
        Assert.assertTrue(PageableResolver.isSupported(request));
    }
    
    @Test
    public void testResolve() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(PageableResolver.PAGE_PARAMETER, "1");
        request.setParameter(PageableResolver.SIZE_PARAMETER, "42");
        request.setParameter(PageableResolver.SORT_PARAMETER, "id,name,ASC");
        
        Pageable pageable = PageableResolver.getPageable(request, User.class);
        Assert.assertEquals(1, pageable.getPageNumber());
        Assert.assertEquals(42, pageable.getPageSize());
        Assert.assertEquals(Direction.ASC, pageable.getSort().getOrderFor("id").getDirection());
        Assert.assertEquals(Direction.ASC, pageable.getSort().getOrderFor("name").getDirection());
        Assert.assertNull(pageable.getSort().getOrderFor("other"));
    }

    @Test
    public void testResolveEntityDefaults() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(PageableResolver.PAGE_PARAMETER, "1");
        
        Pageable pageable = PageableResolver.getPageable(request, EntityWithPageableDefaults.class);
        Assert.assertEquals(1, pageable.getPageNumber());
        Assert.assertEquals(10, pageable.getPageSize());
        Assert.assertEquals(Direction.DESC, pageable.getSort().getOrderFor("id").getDirection());
        Assert.assertEquals(Direction.DESC, pageable.getSort().getOrderFor("name").getDirection());
        Assert.assertNull(pageable.getSort().getOrderFor("other"));
    }
    
    @Test
    public void testResolveGlobalDefaults() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(PageableResolver.PAGE_PARAMETER, "1");
        
        Pageable pageable = PageableResolver.getPageable(request, EntityWithoutPageableDefaults.class);
        Assert.assertEquals(1, pageable.getPageNumber());
        Assert.assertEquals(10, pageable.getPageSize());
        Assert.assertEquals(Direction.ASC, pageable.getSort().getOrderFor("id").getDirection());
    }
    
    @SortingDefault(direction = Direction.DESC, value = { "id", "name" })
    public static class EntityWithPageableDefaults {
        
    }
    
    public static class EntityWithoutPageableDefaults {
        
    }

}

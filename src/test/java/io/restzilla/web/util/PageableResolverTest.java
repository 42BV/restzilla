/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.web.util;

import io.restzilla.SortingDefault;
import io.restzilla.SortingDefault.SortingDefaults;
import io.restzilla.model.User;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
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
    public void testResolveSingleProperty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(PageableResolver.PAGE_PARAMETER, "1");
        request.setParameter(PageableResolver.SIZE_PARAMETER, "42");
        request.addParameter(PageableResolver.SORT_PARAMETER, "id,ASC");
        
        Pageable pageable = PageableResolver.getPageable(request, User.class);
        Assert.assertEquals(1, pageable.getPageNumber());
        Assert.assertEquals(42, pageable.getPageSize());
        
        Iterator<Order> orders = pageable.getSort().iterator();
        assertHasNextOrder(orders, "id", Direction.ASC);
        Assert.assertFalse(orders.hasNext());
    }
    
    @Test
    public void testResolveMultipleProperties() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(PageableResolver.PAGE_PARAMETER, "1");
        request.setParameter(PageableResolver.SIZE_PARAMETER, "42");
        request.addParameter(PageableResolver.SORT_PARAMETER, "id,name,ASC");
        
        Pageable pageable = PageableResolver.getPageable(request, User.class);
        Assert.assertEquals(1, pageable.getPageNumber());
        Assert.assertEquals(42, pageable.getPageSize());
        
        Iterator<Order> orders = pageable.getSort().iterator();
        assertHasNextOrder(orders, "id", Direction.ASC);
        assertHasNextOrder(orders, "name", Direction.ASC);
        Assert.assertFalse(orders.hasNext());
    }
    
    @Test
    public void testResolveMultipleParameters() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(PageableResolver.PAGE_PARAMETER, "1");
        request.setParameter(PageableResolver.SIZE_PARAMETER, "42");
        request.addParameter(PageableResolver.SORT_PARAMETER, "id,ASC");
        request.addParameter(PageableResolver.SORT_PARAMETER, "name,DESC");
        
        Pageable pageable = PageableResolver.getPageable(request, User.class);
        Assert.assertEquals(1, pageable.getPageNumber());
        Assert.assertEquals(42, pageable.getPageSize());
        
        Iterator<Order> orders = pageable.getSort().iterator();
        assertHasNextOrder(orders, "id", Direction.ASC);
        assertHasNextOrder(orders, "name", Direction.DESC);
        Assert.assertFalse(orders.hasNext());
    }
    
    @Test
    public void testResolveMultipleParametersAndProperties() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(PageableResolver.PAGE_PARAMETER, "1");
        request.setParameter(PageableResolver.SIZE_PARAMETER, "42");
        request.addParameter(PageableResolver.SORT_PARAMETER, "id,name,ASC");
        request.addParameter(PageableResolver.SORT_PARAMETER, "other,DESC");
        request.addParameter(PageableResolver.SORT_PARAMETER, "age,ASC");
        
        Pageable pageable = PageableResolver.getPageable(request, User.class);
        Assert.assertEquals(1, pageable.getPageNumber());
        Assert.assertEquals(42, pageable.getPageSize());
        
        Iterator<Order> orders = pageable.getSort().iterator();
        assertHasNextOrder(orders, "id", Direction.ASC);
        assertHasNextOrder(orders, "name", Direction.ASC);
        assertHasNextOrder(orders, "other", Direction.DESC);
        assertHasNextOrder(orders, "age", Direction.ASC);
        Assert.assertFalse(orders.hasNext());
    }

    @Test
    public void testResolveEntityMultipleDefaults() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(PageableResolver.PAGE_PARAMETER, "1");
        
        Pageable pageable = PageableResolver.getPageable(request, EntityWithMultiplePageableDefaults.class);
        Assert.assertEquals(1, pageable.getPageNumber());
        Assert.assertEquals(10, pageable.getPageSize());
        
        Iterator<Order> orders = pageable.getSort().iterator();
        assertHasNextOrder(orders, "name", Direction.ASC);
        assertHasNextOrder(orders, "age", Direction.DESC);
        assertHasNextOrder(orders, "id", Direction.DESC);
        Assert.assertFalse(orders.hasNext());
    }

    @Test
    public void testResolveEntityDefaults() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(PageableResolver.PAGE_PARAMETER, "1");
        
        Pageable pageable = PageableResolver.getPageable(request, EntityWithPageableDefaults.class);
        Assert.assertEquals(1, pageable.getPageNumber());
        Assert.assertEquals(10, pageable.getPageSize());
        
        Iterator<Order> orders = pageable.getSort().iterator();
        assertHasNextOrder(orders, "name", Direction.ASC);
        Assert.assertFalse(orders.hasNext());
    }
    
    @Test
    public void testResolveGlobalDefaults() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(PageableResolver.PAGE_PARAMETER, "1");
        
        Pageable pageable = PageableResolver.getPageable(request, EntityWithoutPageableDefaults.class);
        Assert.assertEquals(1, pageable.getPageNumber());
        Assert.assertEquals(10, pageable.getPageSize());
        
        Iterator<Order> orders = pageable.getSort().iterator();
        assertHasNextOrder(orders, "id", Direction.ASC);
        Assert.assertFalse(orders.hasNext());
    }
    
    private static void assertHasNextOrder(Iterator<Order> orders, String property, Direction direction) {
        Order order = orders.next();
        Assert.assertNotNull("Order should not be null", order);
        Assert.assertEquals(property, order.getProperty());
        Assert.assertEquals(direction, order.getDirection());
    }

    @SortingDefaults({
        @SortingDefault("name"),
        @SortingDefault(value = { "age", "id" }, direction = Direction.DESC)
    })
    public static class EntityWithMultiplePageableDefaults {
        
    }
    
    @SortingDefault("name")
    public static class EntityWithPageableDefaults {
        
    }
    
    public static class EntityWithoutPageableDefaults {
        
    }

}

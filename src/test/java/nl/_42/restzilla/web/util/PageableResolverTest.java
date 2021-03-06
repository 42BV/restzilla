/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.web.util;

import nl._42.restzilla.RestProperties;
import nl._42.restzilla.SortingDefault;
import nl._42.restzilla.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Iterator;

public class PageableResolverTest {

    private final RestProperties properties;

    public PageableResolverTest() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(RestProperties.DEFAULT_SORT_NAME, "id");

        this.properties = new RestProperties(environment);
    }

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
        
        Pageable pageable = PageableResolver.getPageable(request, User.class, properties);
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
        
        Pageable pageable = PageableResolver.getPageable(request, User.class, properties);
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
        
        Pageable pageable = PageableResolver.getPageable(request, User.class, properties);
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
        
        Pageable pageable = PageableResolver.getPageable(request, User.class, properties);
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
        
        Pageable pageable = PageableResolver.getPageable(request, EntityWithMultiplePageableDefaults.class, properties);
        Assert.assertEquals(1, pageable.getPageNumber());
        Assert.assertEquals(10, pageable.getPageSize());
        
        Iterator<Order> orders = pageable.getSort().iterator();
        assertHasNextOrder(orders, "name", Direction.ASC);
        assertHasNextOrder(orders, "age", Direction.DESC);
        assertHasNextOrder(orders, "id", Direction.DESC);
        Assert.assertFalse(orders.hasNext());
    }

    @Test
    public void testResolveExceedingSize() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(PageableResolver.SIZE_PARAMETER, "420");

        Pageable pageable = PageableResolver.getPageable(request, User.class, properties);
        Assert.assertEquals(0, pageable.getPageNumber());
        Assert.assertEquals(properties.getMaxPageSize(), pageable.getPageSize());
    }

    @Test
    public void testResolveEntityDefaults() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(PageableResolver.PAGE_PARAMETER, "1");
        
        Pageable pageable = PageableResolver.getPageable(request, EntityWithPageableDefaults.class, properties);
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
        
        Pageable pageable = PageableResolver.getPageable(request, EntityWithoutPageableDefaults.class, properties);
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

    @SortingDefault.SortingDefaults({
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

/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service.impl;

import io.restzilla.AbstractSpringTest;
import io.restzilla.builder.UserBuilder;
import io.restzilla.model.User;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ReadServiceFinderAdapterTest extends AbstractSpringTest {
    
    @Autowired
    private ReadService readService;
    
    @Autowired
    private UserBuilder userBuilder;

    private ReadServiceFinderAdapter adapter;
    
    @Before
    public void setUp() {
        adapter = new ReadServiceFinderAdapter(readService);
    }
    
    @Test
    public void testFind() {
        User jan = userBuilder.createUser("Jan");
        Object user = adapter.find(jan.getId(), User.class);
        Assert.assertNotNull(user);
        Assert.assertEquals("Jan", ((User) user).getName());
    }

}

/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service;

import io.restzilla.AbstractSpringTest;
import io.restzilla.builder.UserBuilder;
import io.restzilla.model.User;

import io.restzilla.web.mapping.ReadServiceEntityFinder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ReadServiceEntityFinderTest extends AbstractSpringTest {
    
    @Autowired
    private ReadService readService;
    
    @Autowired
    private UserBuilder userBuilder;

    private ReadServiceEntityFinder entityFinder;
    
    @Before
    public void setUp() {
        entityFinder = new ReadServiceEntityFinder(readService);
    }
    
    @Test
    public void testFind() {
        User jan = userBuilder.createUser("Jan");
        Object user = entityFinder.find(jan.getId(), User.class);
        Assert.assertNotNull(user);
        Assert.assertEquals("Jan", ((User) user).getName());
    }

}

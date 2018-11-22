/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.service;

import nl._42.restzilla.AbstractSpringTest;
import nl._42.restzilla.builder.UserBuilder;
import nl._42.restzilla.model.User;

import nl._42.restzilla.web.mapping.ReadServiceEntityFinder;
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

/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.rest;

import nl.mad.rest.builder.UserBuilder;
import nl.mad.rest.model.User;
import nl.mad.rest.model.WithRepository;
import nl.mad.rest.model.WithService;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 
 *
 * @author Jeroen van Schagen
 * @since Aug 24, 2015
 */
public class EnableRestTest extends AbstractControllerTest {

    @Autowired
    private UserBuilder userBuilder;

    @Test
    public void testFindAllNoData() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user");
        request.setMethod(RequestMethod.GET.name());

        MockHttpServletResponse response = call(request);
        Assert.assertEquals("[]", response.getContentAsString());
    }
    
    @Test
    public void testFindAll() throws Exception {
        userBuilder.createUser("Jan");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals("[{\"name\":\"Jan\"}]", response.getContentAsString());
    }
    
    @Test
    public void testFindById() throws Exception {
        User henk = userBuilder.createUser("Henk");
        userBuilder.createUser("Piet");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user/" + henk.getId());
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals("{\"name\":\"Henk\"}", response.getContentAsString());
    }
    
    @Test
    public void testCreate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user");
        request.setMethod(RequestMethod.POST.name());
        
        User piet = new User();
        piet.setName("Piet");
        setContentAsJson(request, piet);

        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("{\"name\":\"Piet\"}", response.getContentAsString());
        
        Assert.assertEquals(Long.valueOf(1), 
                            getJdbcTemplate().queryForObject("SELECT count(*) FROM user", Long.class));
    }
    
    @Test
    public void testUpdate() throws Exception {        
        User henk = userBuilder.createUser("Henk");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user");
        request.setMethod(RequestMethod.PUT.name());
        
        henk.setName("Piet");
        setContentAsJson(request, henk);

        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("{\"name\":\"Piet\"}", response.getContentAsString());
        
        Assert.assertEquals(Long.valueOf(1), 
                            getJdbcTemplate().queryForObject("SELECT count(*) FROM user", Long.class));
    }

    @Test
    public void testDelete() throws Exception {
        User henk = userBuilder.createUser("Henk");
        userBuilder.createUser("Piet");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user/" + henk.getId());
        request.setMethod(RequestMethod.DELETE.name());
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        
        Assert.assertEquals(Long.valueOf(0), 
                            getJdbcTemplate().queryForObject("SELECT count(*) FROM user WHERE id = " + henk.getId(), Long.class));
    }

    // In the tests below we have overwritten our conventional beans

    @Test
    public void testCustomRepository() throws Exception {
        WithRepository entity = new WithRepository();
        entity.setName("Test");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/withrepository");
        request.setMethod(RequestMethod.POST.name());
        setContentAsJson(request, entity);
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("{\"id\":1,\"name\":\"Test\"}", response.getContentAsString());
    }
    
    @Test
    public void testCustomService() throws Exception {
        WithService entity = new WithService();
        entity.setName("Test");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/withservice");
        request.setMethod(RequestMethod.POST.name());
        setContentAsJson(request, entity);
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        // In the service we append an '!' to the name 
        Assert.assertEquals("{\"id\":1,\"name\":\"Test!\"}", response.getContentAsString());
    }

}

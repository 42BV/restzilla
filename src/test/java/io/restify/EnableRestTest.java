/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify;

import io.restify.builder.UserBuilder;
import io.restify.model.User;
import io.restify.model.WithService;
import io.restify.util.PageableResolver;

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
    public void testFindAllAsArray() throws Exception {
        userBuilder.createUser("Jan");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals("[{\"name\":\"Jan\"}]", response.getContentAsString());
    }
    
    @Test
    public void testFindAllAsArrayNoData() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user");
        request.setMethod(RequestMethod.GET.name());

        MockHttpServletResponse response = call(request);
        Assert.assertEquals("[]", response.getContentAsString());
    }
    
    @Test
    public void testFindAllAsPage() throws Exception {
        userBuilder.createUser("Jan");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user");
        request.setParameter(PageableResolver.PAGE_PARAMETER, "0");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        String contents = response.getContentAsString();
        Assert.assertTrue(contents.contains("\"content\":[{\"name\":\"Jan\"}]"));
        Assert.assertTrue(contents.contains("\"number\":0"));
        Assert.assertTrue(contents.contains("\"size\":10"));
    }
    
    @Test
    public void testFindAllAsPageNoData() throws Exception {
        userBuilder.createUser("Jan");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user");
        request.setParameter(PageableResolver.PAGE_PARAMETER, "1");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        String contents = response.getContentAsString();
        Assert.assertTrue(contents.contains("\"content\":[]"));
        Assert.assertTrue(contents.contains("\"number\":1"));
        Assert.assertTrue(contents.contains("\"size\":10"));
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
        Assert.assertTrue(response.getContentAsString().matches("\\d+"));
        
        Assert.assertEquals(Long.valueOf(1), 
                            getJdbcTemplate().queryForObject("SELECT count(*) FROM user", Long.class));
    }
    
    @Test
    public void testUpdate() throws Exception {        
        User henk = userBuilder.createUser("Henk");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user/" + henk.getId());
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
        Assert.assertEquals("", response.getContentAsString());

        Assert.assertEquals(Long.valueOf(0), 
                            getJdbcTemplate().queryForObject("SELECT count(*) FROM user WHERE id = " + henk.getId(), Long.class));
    }

    // Custom beans

    @Test
    public void testCustomRepository() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/withrepository");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("[]", response.getContentAsString());
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
    
    // Custom configuration

    @Test
    public void testDisabled() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/withdisabled");
        request.setMethod(RequestMethod.GET.name());
        
        Assert.assertNull(getHandlerChain(request));
    }
    
    @Test(expected = SecurityException.class)
    public void testSecured() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/withsecurity");
        request.setMethod(RequestMethod.GET.name());
        
        call(request);
    }

}

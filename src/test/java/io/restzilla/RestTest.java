/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla;

import io.restzilla.builder.EntityBuilder;
import io.restzilla.builder.OtherBuilder;
import io.restzilla.builder.UserBuilder;
import io.restzilla.model.User;
import io.restzilla.model.WithOtherEntity;
import io.restzilla.model.WithPatch;
import io.restzilla.model.WithPatchNested;
import io.restzilla.model.WithReadOnly;
import io.restzilla.model.WithRepository;
import io.restzilla.model.WithRollback;
import io.restzilla.model.WithSecurity;
import io.restzilla.model.WithService;
import io.restzilla.model.WithoutPatch;
import io.restzilla.model.dto.ValidationDto;
import io.restzilla.util.PageableResolver;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 
 *
 * @author Jeroen van Schagen
 * @since Aug 24, 2015
 */
public class RestTest extends AbstractControllerTest {

    @Autowired
    private UserBuilder userBuilder;
    
    @Autowired
    private OtherBuilder otherBuilder;
    
    @Autowired
    private EntityBuilder entityBuilder;

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
    public void testCreateAsArray() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user");
        request.setMethod(RequestMethod.POST.name());
        
        User piet = new User();
        piet.setName("Piet");
        User jan = new User();
        jan.setName("Jan");
        setContentAsJson(request, Arrays.asList(piet, jan));

        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        String[] contentsAsString = response.getContentAsString().split(",");
        Assert.assertEquals(2, contentsAsString.length);
        Assert.assertTrue(response.getContentAsString().matches("\\[\\d+\\,\\d+\\]"));
        
        Assert.assertEquals(Long.valueOf(2),
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
    
    //
    // Query
    //
    
    @Test
    public void testFindAllWithQuery() throws Exception {
        WithOtherEntity entity = otherBuilder.createOther("My name");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-other-entity");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("[{\"id\":" + entity.getId() + ",\"name\":\"My name\"}]", response.getContentAsString());
    }
    
    @Test
    public void testFindByIdWithQuery() throws Exception {
        WithOtherEntity entity = otherBuilder.createOther("My name");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-other-entity/" + entity.getId());
        request.setMethod(RequestMethod.GET.name());

        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("{\"id\":" + entity.getId() + ",\"name\":\"My name\"}", response.getContentAsString());
    }
    
    @Test
    public void testUpdateWithQuery() throws Exception {
        WithOtherEntity entity = otherBuilder.createOther("My name");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-other-entity/" + entity.getId());
        request.setMethod(RequestMethod.PUT.name());
        setContentAsJson(request, entity);

        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("{\"id\":" + entity.getId() + ",\"name\":\"My name\"}", response.getContentAsString());
    }

    //
    // Alternate configuration
    //
    
    @Test
    public void testCustomBasePath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/MyBasePath");
        request.setMethod(RequestMethod.GET.name());
        
        Assert.assertNotNull(getHandlerChain(request));
    }
    
    @Test
    public void testNestedBasePath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/mybase/path");
        request.setMethod(RequestMethod.GET.name());
        
        Assert.assertNull(getHandlerChain(request));
    }
    
    @Test
    public void testDuplicate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-duplicate");
        request.setMethod(RequestMethod.GET.name());
        
        Assert.assertNotNull(getHandlerChain(request));
    }
    
    @Test
    public void testDisabled() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-disabled");
        request.setMethod(RequestMethod.GET.name());
        
        Assert.assertNull(getHandlerChain(request));
    }
    
    @Test
    public void testReadOnly() throws Exception {
        WithReadOnly entity = new WithReadOnly();
        entity.setName("Test");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-read-only");
        request.setMethod(RequestMethod.POST.name());
        setContentAsJson(request, entity);
        
        Assert.assertNull(getHandlerChain(request));
    }
    
    @Test
    public void testPagedOnly() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-paged-only");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        String contents = response.getContentAsString();
        Assert.assertTrue(contents.contains("\"content\":[]"));
        Assert.assertTrue(contents.contains("\"number\":0"));
        Assert.assertTrue(contents.contains("\"size\":10"));
    }
    
    @Test
    @Transactional
    public void testPatch() throws Exception {
        WithPatch entity = new WithPatch();
        entity.setName("My name");
        entity.setEmail("email@42.nl");
        entity.setNested(new WithPatchNested());
        entity.getNested().setNestedName("My nested name");
        entity.getNested().setNestedOther("My nested other");
        entityBuilder.save(entity);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-patch/" + entity.getId());
        request.setMethod(RequestMethod.PUT.name());
        
        setValueAsJson(request, "{\"name\":\"New name\",\"nested\":{\"nestedName\":\"New nested name\"}}");
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("{\"id\":" + entity.getId()
                + ",\"name\":\"New name\",\"email\":\"email@42.nl\",\"nested\":{\"nestedName\":\"New nested name\",\"nestedOther\":\"My nested other\"}}",
                response.getContentAsString());
    }
    
    @Test
    @Transactional
    public void testNoPatch() throws Exception {
        WithoutPatch entity = new WithoutPatch();
        entity.setName("My name");
        entity.setName("email@42.nl");
        entityBuilder.save(entity);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/without-patch/" + entity.getId());
        request.setMethod(RequestMethod.PUT.name());
        
        setValueAsJson(request, "{\"name\":\"New name\"}");
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("{\"id\":" + entity.getId() + ",\"name\":\"New name\",\"email\":null}", response.getContentAsString());
    }
    
    //
    // Security
    //
    
    @Test
    public void testSecuredReader() throws Exception {
        TestingAuthenticationToken user = new TestingAuthenticationToken("user", "user", "ROLE_READER");
        SecurityContextHolder.getContext().setAuthentication(user);
        
        try {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/with-security");
            request.setMethod(RequestMethod.GET.name());
            
            MockHttpServletResponse response = call(request);
            Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }
    
    @Test(expected = SecurityException.class)
    public void testSecuredReaderFail() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-security");
        request.setMethod(RequestMethod.GET.name());
        
        call(request);
    }
    
    @Test
    public void testSecuredModify() throws Exception {
        TestingAuthenticationToken admin = new TestingAuthenticationToken("admin", "admin", "ROLE_CHANGER");
        SecurityContextHolder.getContext().setAuthentication(admin);
        
        try {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/with-security");
            request.setMethod(RequestMethod.POST.name());
            
            WithSecurity piet = new WithSecurity();
            piet.setName("Piet");
            setContentAsJson(request, piet);

            MockHttpServletResponse response = call(request);
            Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }
    
    @Test(expected = SecurityException.class)
    public void testSecuredModifyFail() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-security");
        request.setMethod(RequestMethod.GET.name());
        
        call(request);
    }

    @Test
    @Transactional
    public void testSecuredCustom() throws Exception {
        TestingAuthenticationToken admin = new TestingAuthenticationToken("admin", "admin", "ROLE_ADMIN");
        SecurityContextHolder.getContext().setAuthentication(admin);
        
        WithSecurity piet = new WithSecurity();
        piet.setName("Piet");
        entityBuilder.save(piet);

        try {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/with-security/" + piet.getId());
            request.setMethod(RequestMethod.DELETE.name());
            
            MockHttpServletResponse response = call(request);
            Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }
    
    @Test(expected = SecurityException.class)
    public void testSecuredCustomFail() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-security/1");
        request.setMethod(RequestMethod.DELETE.name());
        
        call(request);
    }
    
    @Test
    public void testValidation() throws Exception {
        ValidationDto dto = new ValidationDto();
        dto.name = "Henk";
        dto.street = "Teststreet 42";
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-validation");
        request.setMethod(RequestMethod.POST.name());
        setContentAsJson(request, dto);
        
        MockHttpServletResponse response = call(request);
        String contents = response.getContentAsString();
        Assert.assertTrue(contents.contains("\"name\":\"Henk\""));
        Assert.assertTrue(contents.contains("\"street\":\"Teststreet 42\""));
    }
    
    @Test(expected = BindException.class)
    public void testValidationFail() throws Exception {
        ValidationDto dto = new ValidationDto();
        dto.name = "Henk";
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-validation");
        request.setMethod(RequestMethod.POST.name());
        setContentAsJson(request, dto);
        
        call(request);
    }
    
    //
    // Custom beans
    //

    @Test
    public void testCustomRepository() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-repository");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("[]", response.getContentAsString());
    }
    
    @Test
    public void testCustomRepositoryQuery() throws Exception {
        WithRepository jan = new WithRepository();
        jan.setName("Jan");
        jan.setActive(true);
        entityBuilder.save(jan);
        
        WithRepository henk = new WithRepository();
        henk.setName("Henk");
        henk.setActive(true);
        entityBuilder.save(henk);

        WithRepository piet = new WithRepository();
        piet.setName("Piet");
        entityBuilder.save(piet);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-repository");
        request.setParameter("active", "true");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("[{\"name\":\"Henk\",\"active\":true},{\"name\":\"Jan\",\"active\":true}]", response.getContentAsString());
    }
    
    @Test
    public void testCustomRepositoryQuerySort() throws Exception {
        WithRepository jan = new WithRepository();
        jan.setName("Jan");
        jan.setActive(true);
        entityBuilder.save(jan);
        
        WithRepository henk = new WithRepository();
        henk.setName("Henk");
        henk.setActive(true);
        entityBuilder.save(henk);

        WithRepository piet = new WithRepository();
        piet.setName("Piet");
        entityBuilder.save(piet);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-repository");
        request.setParameter("active", "true");
        request.setParameter(PageableResolver.SORT_PARAMETER, "name,desc");
        request.setMethod(RequestMethod.GET.name());
        
        // Returned in reverse order
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("[{\"name\":\"Jan\",\"active\":true},{\"name\":\"Henk\",\"active\":true}]", response.getContentAsString());
    }
    
    @Test
    public void testCustomRepositoryQueryPage() throws Exception {
        WithRepository jan = new WithRepository();
        jan.setName("Jan");
        jan.setActive(true);
        entityBuilder.save(jan);
        
        WithRepository piet = new WithRepository();
        piet.setName("Piet");
        entityBuilder.save(piet);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-repository");
        request.setParameter("active", "true");
        request.setParameter(PageableResolver.PAGE_PARAMETER, "0");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        String contents = response.getContentAsString();
        Assert.assertTrue(contents.contains("\"content\":[{\"name\":\"Jan\",\"active\":true}]"));
        Assert.assertTrue(contents.contains("\"number\":0"));
        Assert.assertTrue(contents.contains("\"size\":10"));
    }

    @Test
    public void testCustomRepositoryQueryWithExactParameter() throws Exception {
        WithRepository jan = new WithRepository();
        jan.setName("Jan");
        jan.setActive(true);
        entityBuilder.save(jan);
        
        WithRepository henk = new WithRepository();
        henk.setName("Henk");
        henk.setActive(true);
        entityBuilder.save(henk);
        
        WithRepository piet = new WithRepository();
        piet.setName("Piet");
        entityBuilder.save(piet);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-repository");
        request.setParameter("active", "true");
        request.setParameter("type", "name");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("[{\"name\":\"Henk\"},{\"name\":\"Jan\"}]", response.getContentAsString());
    }
    
    @Test
    public void testCustomRepositoryQuerySingleResult() throws Exception {
        WithRepository jan = new WithRepository();
        jan.setName("Jan");
        jan.setActive(true);
        entityBuilder.save(jan);
        
        WithRepository piet = new WithRepository();
        piet.setName("Piet");
        entityBuilder.save(piet);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-repository");
        request.setParameter("active", "true");
        request.setParameter("unique", "true");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("{\"name\":\"Jan\"}", response.getContentAsString());
    }

    @Test
    public void testCustomService() throws Exception {
        WithService entity = new WithService();
        entity.setName("Test");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-service");
        request.setMethod(RequestMethod.POST.name());
        setContentAsJson(request, entity);
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        // In the service we append ' with User!' to the name 
        Assert.assertEquals("{\"id\":1,\"name\":\"Test with User!\"}", response.getContentAsString());
    }
    
    @Test
    public void testCustomServiceRollback() throws Exception {
        WithRollback entity = new WithRollback();
        entity.setName("Initial");
        entityBuilder.save(entity);

        // Change name, and enforce rollback
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-rollback/" + entity.getId());
        request.setMethod(RequestMethod.PUT.name());
        setValueAsJson(request, "{\"name\":\"Updated\"}");
        
        try {
            call(request);
            Assert.fail("Expected an UnsupportedOperationException.");
        } catch (UnsupportedOperationException uoe) {
        } catch (RuntimeException rte) {
            Assert.fail("Expected an UnsupportedOperationException.");
        }

        WithRollback result = entityBuilder.get(WithRollback.class, entity.getId());
        Assert.assertEquals("Initial", result.getName());
    }
    
    @Test
    public void testCustomServiceWithFinder() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-service");
        request.setMethod(RequestMethod.GET.name());
        request.setParameter("custom", "true");
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("[{\"id\":42,\"name\":\"Service generated\"}]", response.getContentAsString());
    }
    
    // TODO: Figure out why this causes an exception
    @Test(expected = IllegalArgumentException.class)
    public void testCustomServiceWithProxy() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-proxy-service");
        request.setMethod(RequestMethod.GET.name());
        request.setParameter("age", "42");
        
        call(request);
    }
    
    @Test
    public void testCustomController() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/with-controller");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("[]", response.getContentAsString());
    }

}

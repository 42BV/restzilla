/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.NestedServletException;

import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    public void testFindAllAsArray() throws Exception {
        userBuilder.createUser("Jan");
        
        this.webClient.perform(get("/user/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Jan"));
    }
    
    @Test
    public void testFindAllAsArrayNoData() throws Exception {
        this.webClient.perform(get("/user/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").doesNotExist());
    }
        
    @Test
    public void testFindAllAsPage() throws Exception {
        userBuilder.createUser("Jan");
        
        this.webClient.perform(get("/user?page=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.content[0].name").value("Jan"));
    }
    
    @Test
    public void testFindAllAsPageNoData() throws Exception {
        userBuilder.createUser("Jan");
        
        this.webClient.perform(get("/user?page=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.content[0]").doesNotExist());
    }
    
    @Test
    public void testFindById() throws Exception {
        User henk = userBuilder.createUser("Henk");
        userBuilder.createUser("Piet");
        
        this.webClient.perform(get("/user/" + henk.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Henk"));
    }
    
    @Test
    public void testCreate() throws Exception {
        User piet = new User();
        piet.setName("Piet");
        
        this.webClient.perform(post("/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(piet)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").exists());
        
        Assert.assertEquals(Long.valueOf(1), 
                            getJdbcTemplate().queryForObject("SELECT count(*) FROM user", Long.class));
    }
    
    @Test
    public void testCreateAsArray() throws Exception {
        User piet = new User();
        piet.setName("Piet");
        User jan = new User();
        jan.setName("Jan");
        
        this.webClient.perform(post("/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Arrays.asList(piet, jan))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray());

        Assert.assertEquals(Long.valueOf(2),
                            getJdbcTemplate().queryForObject("SELECT count(*) FROM user", Long.class));
    }
    
    @Test
    public void testUpdate() throws Exception {        
        User henk = userBuilder.createUser("Henk");

        henk.setName("Piet");
        
        this.webClient.perform(put("/user/" + henk.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(henk)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value("Piet"));

        Assert.assertEquals(Long.valueOf(1), 
                            getJdbcTemplate().queryForObject("SELECT count(*) FROM user", Long.class));
    }

    @Test
    public void testDelete() throws Exception {
        User henk = userBuilder.createUser("Henk");
        userBuilder.createUser("Piet");
        
        this.webClient.perform(delete("/user/" + henk.getId()))
                        .andExpect(status().isOk());
        
        Assert.assertEquals(Long.valueOf(0), 
                            getJdbcTemplate().queryForObject("SELECT count(*) FROM user WHERE id = " + henk.getId(), Long.class));
    }
    
    //
    // Query
    //
    
    @Test
    public void testFindAllWithQuery() throws Exception {
        WithOtherEntity entity = otherBuilder.createOther("My name");
        
        this.webClient.perform(get("/with-other-entity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(entity.getId().intValue()))
                .andExpect(jsonPath("$[0].name").value("My name"));
    }
    
    @Test
    public void testFindByIdWithQuery() throws Exception {
        WithOtherEntity entity = otherBuilder.createOther("My name"); 

        this.webClient.perform(get("/with-other-entity/" + entity.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entity.getId().intValue()))
                .andExpect(jsonPath("$.name").value("My name"));
    }
    
    @Test
    public void testUpdateWithQuery() throws Exception {
        WithOtherEntity entity = otherBuilder.createOther("My name");
        
        this.webClient.perform(put("/with-other-entity/" + entity.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(entity)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(entity.getId().intValue()))
                        .andExpect(jsonPath("$.name").value("My name"));
    }

    //
    // Alternate configuration
    //
    
    @Test
    public void testCustomBasePath() throws Exception {
        this.webClient.perform(get("/MyBasePath"))
                .andExpect(status().isOk());
    }
    
    @Test
    public void testNestedBasePath() throws Exception {
        this.webClient.perform(get("/mybase/path"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void testDuplicate() throws Exception {
        this.webClient.perform(get("/with-duplicate"))
                .andExpect(status().isOk());
    }
    
    @Test
    public void testDisabled() throws Exception {
        this.webClient.perform(get("/with-disabled"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void testReadOnly() throws Exception {
        WithReadOnly entity = new WithReadOnly();
        entity.setName("Test");
        
        this.webClient.perform(post("/with-read-only/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(entity)))
                        .andExpect(status().isNotFound());
    }
    
    @Test
    public void testPagedOnly() throws Exception {
        this.webClient.perform(get("/with-paged-only"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }
    
    // TODO: This should not cause an exception
    @Transactional
    @Test(expected = NestedServletException.class)
    public void testPatch() throws Exception {
        WithPatch entity = new WithPatch();
        entity.setName("My name");
        entity.setEmail("email@42.nl");
        entity.setNested(new WithPatchNested());
        entity.getNested().setNestedName("My nested name");
        entity.getNested().setNestedOther("My nested other");
        
        entityBuilder.save(entity);
        
        this.webClient.perform(patch("/with-patch/" + entity.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"New name\",\"nested\":{\"nestedName\":\"New nested name\"}}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value("New name"))
                        .andExpect(jsonPath("$.email").value("email@42.nl"))
                        .andExpect(jsonPath("$.nested.nestedName").value("New nested name"))
                        .andExpect(jsonPath("$.nested.nestedOther").value("My nested other"));
    }
    
    @Test
    @Transactional
    public void testNoPatch() throws Exception {
        WithoutPatch entity = new WithoutPatch();
        entity.setName("My name");
        entity.setName("email@42.nl");
        entityBuilder.save(entity);
        
        this.webClient.perform(put("/without-patch/" + entity.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"New name\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value("New name"))
                        .andExpect(jsonPath("$.email").doesNotExist());
    }
    
    //
    // Security
    //
    
    @Test
    public void testSecuredReader() throws Exception {
        TestingAuthenticationToken user = new TestingAuthenticationToken("user", "user", "ROLE_READER");
        
        this.webClient.perform(get("/with-security").principal(user))
                        .andExpect(status().isOk());
    }
    
    @Test(expected = NestedServletException.class)
    public void testSecuredReaderFail() throws Exception {
        this.webClient.perform(get("/with-security"))
                        .andExpect(status().isOk());
    }
    
    @Test
    public void testSecuredModify() throws Exception {
        TestingAuthenticationToken user = new TestingAuthenticationToken("admin", "admin", "ROLE_CHANGER");
        
        WithSecurity piet = new WithSecurity();
        piet.setName("Piet");
        
        this.webClient.perform(post("/with-security")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(piet))
                            .principal(user))
                        .andExpect(status().isOk());
    }
    
    @Test(expected = NestedServletException.class)
    public void testSecuredModifyFail() throws Exception {
        WithSecurity piet = new WithSecurity();
        piet.setName("Piet");
        
        this.webClient.perform(post("/with-security")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(piet)))
                        .andExpect(status().isOk());
    }
    
    @Test
    @Transactional
    public void testSecuredCustom() throws Exception {
        TestingAuthenticationToken user = new TestingAuthenticationToken("admin", "admin", "ROLE_ADMIN");
        
        WithSecurity piet = new WithSecurity();
        piet.setName("Piet");
        entityBuilder.save(piet);
        
        this.webClient.perform(delete("/with-security/" + piet.getId()).principal(user))
            .andExpect(status().isOk());

    }
    
    @Test(expected = NestedServletException.class)
    @Transactional
    public void testSecuredCustomFail() throws Exception {
        WithSecurity piet = new WithSecurity();
        piet.setName("Piet");
        entityBuilder.save(piet);
        
        this.webClient.perform(delete("/with-security/" + piet.getId()))
            .andExpect(status().isOk());
    }
        
    //
    // Validation
    //

    @Test
    public void testValidation() throws Exception {
        ValidationDto dto = new ValidationDto();
        dto.name = "Henk";
        dto.street = "Teststreet 42";
        
        this.webClient.perform(post("/with-validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value("Henk"))
                        .andExpect(jsonPath("$.street").value("Teststreet 42"));
    }
    
    @Test
    public void testValidationFail() throws Exception {
        ValidationDto dto = new ValidationDto();
        dto.name = "Henk";
        
        this.webClient.perform(post("/with-validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().is4xxClientError());
    }
        
    //
    // Custom beans
    //

    @Test
    public void testCustomRepository() throws Exception {
        this.webClient.perform(get("/with-repository"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray());
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
        
        this.webClient.perform(get("/with-repository?active=true"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$[0].name").value("Henk"))
                        .andExpect(jsonPath("$[1].name").value("Jan"));
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
        
        this.webClient.perform(get("/with-repository?active=true&sort=name,desc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$[0].name").value("Jan"))
                        .andExpect(jsonPath("$[1].name").value("Henk"));
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
        
        this.webClient.perform(get("/with-repository?active=true&page=0"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.number").value(0))
                        .andExpect(jsonPath("$.size").value(10))
                        .andExpect(jsonPath("$.content").isArray())
                        .andExpect(jsonPath("$.content[0].name").value("Jan"));
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
        
        this.webClient.perform(get("/with-repository?active=true&type=name"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$[0].name").value("Henk"))
                        .andExpect(jsonPath("$[1].name").value("Jan"));
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
        
        this.webClient.perform(get("/with-repository?active=true&unique=true"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value("Jan"));
    }

    @Test
    public void testCustomService() throws Exception {
        WithService entity = new WithService();
        entity.setName("Test");
        
        this.webClient.perform(post("/with-service")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(entity)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(1))
                        .andExpect(jsonPath("$.name").value("Test with User!"));
    }
    
    @Test
    public void testCustomServiceRollback() throws Exception {
        WithRollback entity = new WithRollback();
        entity.setName("Initial");
        entityBuilder.save(entity);

        try {
            this.webClient.perform(put("/with-rollback/" + entity.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Updated\"}"))
                .andExpect(status().is5xxServerError());
            
            Assert.fail("Expected an UnsupportedOperationException.");
        } catch (NestedServletException nse) {
            Assert.assertEquals(UnsupportedOperationException.class, nse.getCause().getClass());
        }

        WithRollback result = entityBuilder.get(WithRollback.class, entity.getId());
        Assert.assertEquals("Initial", result.getName());
    }
    
    @Test
    public void testCustomServiceWithFinder() throws Exception {
        this.webClient.perform(get("/with-service?custom=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(42))
                .andExpect(jsonPath("$[0].name").value("Service generated"));
    }
        
    // TODO: Figure out why this causes an exception
    @Test(expected = NestedServletException.class)
    public void testCustomServiceWithProxy() throws Exception {
        this.webClient.perform(get("/with-proxy-service?age=42"))
                .andExpect(status().isOk());
    }
    
    @Test
    public void testCustomController() throws Exception {
        this.webClient.perform(get("/with-controller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
                //.andExpect(jsonPath("$.a").value("b"));
    }
    
    @Test
    public void testCustomControllerEmptyMapping() throws Exception {
        this.webClient.perform(get("/with-controller-empty-mapping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    public void testCustomControllerCustomMapping() throws Exception {
        this.webClient.perform(get("/with-controller-custom-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

}

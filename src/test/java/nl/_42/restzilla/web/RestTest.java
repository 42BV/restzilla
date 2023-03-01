/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import nl._42.restzilla.AbstractControllerTest;
import nl._42.restzilla.builder.EntityBuilder;
import nl._42.restzilla.builder.OtherBuilder;
import nl._42.restzilla.builder.UserBuilder;
import nl._42.restzilla.model.User;
import nl._42.restzilla.model.WithOtherEntity;
import nl._42.restzilla.model.WithPatch;
import nl._42.restzilla.model.WithPatchNested;
import nl._42.restzilla.model.WithReadOnly;
import nl._42.restzilla.model.WithRepository;
import nl._42.restzilla.model.WithRollback;
import nl._42.restzilla.model.WithSecurity;
import nl._42.restzilla.model.WithService;
import nl._42.restzilla.model.WithoutPatch;
import nl._42.restzilla.model.dto.ValidationDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @PersistenceContext
    private EntityManager entityManager;

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

        Assertions.assertEquals(Long.valueOf(1), getJdbcTemplate().queryForObject("SELECT count(*) FROM user", Long.class));
    }

    @Test
    public void testCreateAsArray() throws Exception {
        User piet = new User();
        piet.setName("Piet");

        User jan = new User();
        jan.setName("Jan");

        this.webClient.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(piet, jan))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());

        Assertions.assertEquals(Long.valueOf(2), getJdbcTemplate().queryForObject("SELECT count(*) FROM user", Long.class));
    }

    @Test
    public void testUpdate() throws Exception {
        User henk = userBuilder.createUser("Henk");
        henk.setName("Piet");

        this.webClient.perform(put("/user/{id}", henk.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(henk)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Piet"));


        User updated = entityManager.find(User.class, henk.getId());
        Assertions.assertNotNull(updated);
        Assertions.assertEquals("Piet", updated.getName());
    }

    @Test
    public void testDelete() throws Exception {
        User henk = userBuilder.createUser("Henk");
        userBuilder.createUser("Piet");
        
        this.webClient.perform(delete("/user/{id}", henk.getId()))
            .andExpect(status().isOk());

        User deleted = entityManager.find(User.class, henk.getId());
        Assertions.assertNull(deleted);
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

        this.webClient.perform(patch("/with-patch/{id}", entity.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New name\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("New name"))
            .andExpect(jsonPath("$.email").value("email@42.nl"))
            .andExpect(jsonPath("$.nested.nestedName").value("My nested name"))
            .andExpect(jsonPath("$.nested.nestedOther").value("My nested other"));
    }

    @Test
    @Transactional
    @Disabled("Disabled due to bug in bean mapper, nested patch does not work")
    public void testPatchNested() throws Exception {
        WithPatch entity = new WithPatch();
        entity.setName("My name");
        entity.setEmail("email@42.nl");
        entity.setNested(new WithPatchNested());
        entity.getNested().setNestedName("My nested name");
        entity.getNested().setNestedOther("My nested other");

        entityBuilder.save(entity);

        this.webClient.perform(patch("/with-patch/{id}", entity.getId())
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
    
    @Test
    public void testSecuredReaderFail() throws Exception {
        this.webClient.perform(get("/with-security"))
            .andExpect(status().isForbidden());
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
    
    @Test
    public void testSecuredModifyFail() throws Exception {
        WithSecurity piet = new WithSecurity();
        piet.setName("Piet");
        
        this.webClient.perform(post("/with-security")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(piet)))
            .andExpect(status().isForbidden());
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
    
    @Test
    @Transactional
    public void testSecuredCustomFail() throws Exception {
        WithSecurity piet = new WithSecurity();
        piet.setName("Piet");
        entityBuilder.save(piet);
        
        this.webClient.perform(delete("/with-security/" + piet.getId()))
            .andExpect(status().isForbidden());
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

        this.webClient.perform(put("/with-rollback/" + entity.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated\"}"))
            .andExpect(status().is5xxServerError());

        WithRollback result = entityBuilder.get(WithRollback.class, entity.getId());
        Assertions.assertEquals("Initial", result.getName());
    }

    @Test
    public void testCustomController() throws Exception {
        this.webClient.perform(get("/with-controller"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
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

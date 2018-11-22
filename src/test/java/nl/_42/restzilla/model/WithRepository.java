package nl._42.restzilla.model;

import nl._42.restzilla.RestQuery;
import nl._42.restzilla.RestResource;
import nl._42.restzilla.SortingDefault;
import nl._42.restzilla.model.dto.WithRepositoryDto;
import nl._42.restzilla.model.dto.WithRepositoryNameOnlyDto;

import javax.persistence.Entity;

@Entity
@SortingDefault("name")
@RestResource(queries = {
  @RestQuery(parameters = { "active", "type=name" }, method = "findAllByActive", resultType = WithRepositoryNameOnlyDto.class),
  @RestQuery(parameters = { "active", "unique=true" }, method = "findByActive", unique = true, resultType = WithRepositoryNameOnlyDto.class),
  @RestQuery(parameters = "active", method = "findAllByActive", resultType = WithRepositoryDto.class)
})
public class WithRepository extends BaseEntity {
    
    private String name;
    
    private boolean active;

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }

}

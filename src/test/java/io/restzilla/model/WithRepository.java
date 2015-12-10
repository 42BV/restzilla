package io.restzilla.model;

import io.restzilla.RestQuery;
import io.restzilla.RestResource;
import io.restzilla.SortingDefault;
import io.restzilla.model.dto.WithRepositoryDto;

import javax.persistence.Entity;

@Entity
@SortingDefault("name")
@RestResource(
    queries = @RestQuery(parameters = "active", method = "findAllByActive", resultType = WithRepositoryDto.class)
)
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

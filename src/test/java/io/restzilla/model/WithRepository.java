package io.restzilla.model;

import io.restzilla.RestQuery;
import io.restzilla.RestResource;

import javax.persistence.Entity;

@Entity
@RestResource(
    // TODO: findAllByActive(b,c,a)
    // TODO: Prefer page queries when paged, prefer sorted when sorted.
    // TODO: Cache the found methods
    queries = @RestQuery(parameters = "active", method = "findAllByActive")
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

package nl._42.restzilla.model;

import nl._42.restzilla.RestResource;
import nl._42.restzilla.SortingDefault;

import jakarta.persistence.Entity;

@Entity
@SortingDefault("name")
@RestResource
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

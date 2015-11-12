package io.restzilla.model;

import io.restzilla.RestResource;

import javax.persistence.Entity;

@Entity
@RestResource
public class WithRepository extends BaseEntity {
    
    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

}
package io.restify.model;

import io.restify.RestEnable;

import javax.persistence.Entity;

@Entity
@RestEnable
public class WithRepository extends BaseEntity {
    
    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

}

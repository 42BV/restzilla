package io.restify.model;

import io.restify.EnableRest;

import javax.persistence.Entity;

@Entity
@EnableRest
public class WithRepository extends BaseEntity {
    
    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

}

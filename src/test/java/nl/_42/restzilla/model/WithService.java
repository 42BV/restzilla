/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.model;

import nl._42.restzilla.RestResource;

import jakarta.persistence.Entity;

@Entity
@RestResource
public class WithService extends BaseEntity {
    
    private String name;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

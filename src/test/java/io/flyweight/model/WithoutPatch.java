/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.flyweight.model;

import io.flyweight.RestConfig;
import io.flyweight.RestEnable;

import javax.persistence.Entity;

@Entity
@RestEnable(update = @RestConfig(patch = false))
public class WithoutPatch extends BaseEntity {
    
    private String name;
    
    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

}

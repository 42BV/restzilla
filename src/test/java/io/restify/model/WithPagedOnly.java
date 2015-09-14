/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.model;

import io.restify.RestEnable;

import javax.persistence.Entity;

@Entity
@RestEnable(pagedOnly = true)
public class WithPagedOnly extends BaseEntity {
    
    private String name;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

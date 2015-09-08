/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.model;

import io.restify.CrudConfig;
import io.restify.EnableRest;

import javax.persistence.Entity;

@Entity
@EnableRest(
    findAll = @CrudConfig(enabled = false),
    findOne = @CrudConfig(enabled = false),
     create = @CrudConfig(enabled = false),
     update = @CrudConfig(enabled = false),
     delete = @CrudConfig(enabled = false)
)
public class WithoutEnabled extends BaseEntity {
    
    private String name;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

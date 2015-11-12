/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.model;

import io.restzilla.RestConfig;
import io.restzilla.RestResource;

import javax.persistence.Entity;

@Entity
@RestResource(
    findAll = @RestConfig(enabled = false),
    findOne = @RestConfig(enabled = false),
     create = @RestConfig(enabled = false),
     update = @RestConfig(enabled = false),
     delete = @RestConfig(enabled = false)
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

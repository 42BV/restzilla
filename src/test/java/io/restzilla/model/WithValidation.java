/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.model;

import io.restzilla.RestConfig;
import io.restzilla.RestResource;
import io.restzilla.model.dto.ValidationDto;

import javax.persistence.Entity;

@Entity
@RestResource(create = @RestConfig(inputType = ValidationDto.class))
public class WithValidation extends BaseEntity {

    private String name;
    
    private String street;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getStreet() {
        return street;
    }
    
    public void setStreet(String street) {
        this.street = street;
    }

}

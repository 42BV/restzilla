/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.model;

import nl._42.restzilla.RestConfig;
import nl._42.restzilla.RestResource;
import nl._42.restzilla.model.dto.ValidationDto;

import jakarta.persistence.Entity;

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

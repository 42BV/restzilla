/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.model;

import nl._42.restzilla.RestConfig;
import nl._42.restzilla.RestResource;

import javax.persistence.Entity;

@Entity
@RestResource(readOnly = true, create = @RestConfig(enabled = true))
public class WithReadOnly extends BaseEntity {
    
    private String name;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

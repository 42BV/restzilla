/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.model;

import io.restzilla.RestConfig;
import io.restzilla.RestResource;

import javax.persistence.Entity;

@Entity
@RestResource(
    readSecured = "hasRole('ROLE_READER')",
    modifySecured = "hasRole('ROLE_CHANGER')",
    delete = @RestConfig(secured = "hasRole('ROLE_ADMIN')")
)
public class WithSecurity extends BaseEntity {
    
    private String name;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

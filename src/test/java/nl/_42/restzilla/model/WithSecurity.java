/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.model;

import nl._42.restzilla.RestConfig;
import nl._42.restzilla.RestResource;
import nl._42.restzilla.RestSecured;

import javax.persistence.Entity;

@Entity
@RestResource(
  secured = @RestSecured(
    read = "hasRole('ROLE_READER')",
    modify = "hasRole('ROLE_CHANGER')"
  ),
  delete = @RestConfig(
    secured = "hasRole('ROLE_ADMIN')"
  )
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

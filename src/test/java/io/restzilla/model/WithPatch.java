/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.model;

import io.restzilla.RestResource;

import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
@RestResource(patch = true)
public class WithPatch extends BaseEntity {
    
    private String name;
    
    private String email;
    
    @Embedded
    private WithPatchNested nested;

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

    public WithPatchNested getNested() {
        return nested;
    }
    
    public void setNested(WithPatchNested nested) {
        this.nested = nested;
    }

}

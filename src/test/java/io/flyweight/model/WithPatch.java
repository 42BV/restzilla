/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.flyweight.model;

import io.flyweight.RestConfig;
import io.flyweight.RestEnable;

import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
@RestEnable(update = @RestConfig(patch = true))
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

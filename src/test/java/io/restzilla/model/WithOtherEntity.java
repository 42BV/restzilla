/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.model;

import io.restzilla.RestConfig;
import io.restzilla.RestEnable;

import javax.persistence.Entity;

@Entity
@RestEnable(
    findAll = @RestConfig(resultType = OtherEntity.class, resultByQuery = true),
    findOne = @RestConfig(resultType = OtherEntity.class, resultByQuery = true),
    update = @RestConfig(resultType = OtherEntity.class, resultByQuery = true)
)
public class WithOtherEntity extends BaseEntity {
    
    private String otherName;
    
    public String getOtherName() {
        return otherName;
    }
    
    public void setOtherName(String otherName) {
        this.otherName = otherName;
    }

}

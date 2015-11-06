/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.flyweight.model;

import static io.flyweight.RestMappingStrategy.QUERY;
import io.flyweight.RestConfig;
import io.flyweight.RestEnable;

import javax.persistence.Entity;

@Entity
@RestEnable(
    findAll = @RestConfig(resultType = OtherEntity.class, strategy = QUERY),
    findOne = @RestConfig(resultType = OtherEntity.class, strategy = QUERY),
    update = @RestConfig(resultType = OtherEntity.class, strategy = QUERY)
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

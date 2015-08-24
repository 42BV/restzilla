/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.rest.model;

import javax.persistence.Entity;

import nl.mad.rest.EnableRest;

@Entity
@EnableRest
public class WithService extends BaseEntity {
    
    private String name;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

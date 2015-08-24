/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.rest.model;

import javax.persistence.Entity;

import nl.mad.rest.EnableRest;

/**
 * Represents a product.
 *
 * @since Mar 11, 2015
 */
@Entity
@EnableRest
public class Product extends BaseEntity {
    
    private String name;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

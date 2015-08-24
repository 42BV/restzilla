/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.rest.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 *
 * @author Jeroen van Schagen
 * @since Jun 16, 2015
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUserModel {
    
    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

}

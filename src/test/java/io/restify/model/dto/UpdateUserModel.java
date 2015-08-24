/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 *
 * @author Jeroen van Schagen
 * @since Jun 16, 2015
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateUserModel {
    
    private Long id;

    private String name;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

}

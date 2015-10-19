/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.flyweight.handler.swagger;

/**
 * Defines that this instance is able to describe all APIs to Swagger.
 *
 * @author Jeroen van Schagen
 * @since Sep 3, 2015
 */
public interface SwaggerApiDescriptor {
    
    /**
     * Enhance our API listing with new mappings.
     * 
     * @param listing the API listing
     * @param modelProvider describes our models
     */
    void enhance(com.mangofactory.swagger.models.dto.ApiListing listing, 
                 com.mangofactory.swagger.models.ModelProvider modelProvider);

}

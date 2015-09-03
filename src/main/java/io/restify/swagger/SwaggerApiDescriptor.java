/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.swagger;

/**
 * Defines that this instance is able to describe all APIs to Swagger.
 *
 * @author Jeroen van Schagen
 * @since Sep 3, 2015
 */
public interface SwaggerApiDescriptor {
    
    /**
     * Add any API descriptions.
     * 
     * @param apiListing the API listing
     */
    void addApiDescriptions(com.mangofactory.swagger.models.dto.ApiListing apiListing);

}

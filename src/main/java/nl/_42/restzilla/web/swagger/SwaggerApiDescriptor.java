/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.web.swagger;

import com.mangofactory.swagger.models.ModelProvider;
import com.mangofactory.swagger.models.dto.ApiListing;
import nl._42.restzilla.web.RestInformation;

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
     * @param information the resource information
     * @param listing the API listing
     * @param modelProvider describes our models
     */
    void enhance(
      RestInformation information,
      ApiListing listing,
      ModelProvider modelProvider
    );

}

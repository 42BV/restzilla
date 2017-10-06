package io.restzilla.web;

import io.restzilla.RestInformation;

/**
 * Responsible for building the rest handlers.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public interface ResourceHandlerMappingFactory {
    
    /**
     * Build a new handler mapping.
     * 
     * @param information the REST information
     * @return the handler mapping
     */
    ResourceHandlerMapping build(RestInformation information);
    
}

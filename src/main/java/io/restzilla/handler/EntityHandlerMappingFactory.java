package io.restzilla.handler;

import io.restzilla.RestInformation;

/**
 * Responsible for building the rest handlers.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public interface EntityHandlerMappingFactory {
    
    /**
     * Build a new handler mapping.
     * 
     * @param information the REST information
     * @return the handler mapping
     */
    EntityHandlerMapping build(RestInformation information);
    
}

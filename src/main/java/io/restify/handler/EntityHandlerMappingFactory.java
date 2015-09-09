package io.restify.handler;

import io.restify.RestInformation;
import io.restify.service.CrudService;

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
     * @param service the service
     * @param information the entity information
     * @return the handler mapping
     */
    EntityHandlerMapping build(CrudService<?, ?> service, RestInformation information);
    
}

package io.restify.handler;

import io.restify.EntityInformation;
import io.restify.service.CrudService;

/**
 * Responsible for building the rest handlers.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public interface CrudHandlerMappingFactory {
    
    /**
     * Build a new handler mapping.
     * 
     * @param service the service
     * @param information the entity information
     * @return the handler mapping
     */
    PublicHandlerMapping build(CrudService<?, ?> service, EntityInformation information);
    
}

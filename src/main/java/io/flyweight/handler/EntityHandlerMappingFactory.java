package io.flyweight.handler;

import io.flyweight.RestInformation;
import io.flyweight.service.CrudService;

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
     * @param information the REST information
     * @return the handler mapping
     */
    EntityHandlerMapping build(CrudService<?, ?> service, RestInformation information);
    
}

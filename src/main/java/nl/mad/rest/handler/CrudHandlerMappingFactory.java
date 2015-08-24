package nl.mad.rest.handler;

import nl.mad.rest.EntityInformation;
import nl.mad.rest.service.CrudService;

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
    CrudHandlerMapping build(CrudService<?, ?> service, EntityInformation information);
    
}

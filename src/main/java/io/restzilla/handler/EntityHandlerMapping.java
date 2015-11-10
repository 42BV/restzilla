package io.restzilla.handler;

import io.restzilla.RestInformation;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.handler.AbstractHandlerMapping;

/**
 * Handler mapping that exposes the handler internal method.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public abstract class EntityHandlerMapping extends AbstractHandlerMapping {
    
    /**
     * The REST information.
     */
    private final RestInformation information;
    
    /**
     * Construct a new entity handler mapping.
     * 
     * @param information the REST information
     */
    public EntityHandlerMapping(RestInformation information) {
        this.information = information;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Object getHandlerInternal(HttpServletRequest request) throws Exception;

    /**
     * Retrieve the REST information.
     * 
     * @return the REST information
     */
    public RestInformation getInformation() {
        return information;
    }

}
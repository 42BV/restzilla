package io.restify.handler;

import io.restify.RestInformation;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.handler.AbstractHandlerMapping;

/**
 * Handler mapping that exposes the handler internal method.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public abstract class EntityHandlerMapping extends AbstractHandlerMapping {
    
    private final RestInformation information;
    
    public EntityHandlerMapping(RestInformation information) {
        this.information = information;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Object getHandlerInternal(HttpServletRequest request) throws Exception;

    /**
     * Retrieve the entity information.
     * 
     * @return the information
     */
    public RestInformation getInformation() {
        return information;
    }

}
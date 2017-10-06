package io.restzilla.web;

import io.restzilla.RestInformation;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

/**
 * Handler mapping that exposes the handler internal method.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public abstract class ResourceHandlerMapping extends AbstractHandlerMapping {
    
    /**
     * The REST information.
     */
    private final RestInformation information;
    
    /**
     * Construct a new entity handler mapping.
     * @param information the REST information
     */
    public ResourceHandlerMapping(RestInformation information) {
        this.information = information;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Object getHandlerInternal(HttpServletRequest request) throws Exception;

    /**
     * Describe the generated REST mappings.
     * @param logger the logger to output our description
     */
    public void describe(Logger logger) {
        logger.info("Registered REST resource /{} [{}]", information.getBasePath(), information.getEntityClass().getName());
    }

    /**
     * Retrieve the REST information.
     * @return the REST information
     */
    public RestInformation getInformation() {
        return information;
    }

}
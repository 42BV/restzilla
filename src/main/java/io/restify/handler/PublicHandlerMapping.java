package io.restify.handler;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.handler.AbstractHandlerMapping;

/**
 * Handler mapping that exposes the handler internal method.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public abstract class PublicHandlerMapping extends AbstractHandlerMapping {

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Object getHandlerInternal(HttpServletRequest request) throws Exception;

}
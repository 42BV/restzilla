package io.restify.handler;

import io.restify.UrlUtils;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.PriorityOrdered;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Root handler mapping for managing CRUD requests.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class RootCrudHandlerMapping extends AbstractHandlerMapping implements PriorityOrdered {
    
    private Map<String, PublicHandlerMapping> handlerMappings = new HashMap<String, PublicHandlerMapping>();

    private final RequestMappingHandlerMapping requestHandlerMapping;

    /**
     * Create a new handler mapping.
     * 
     * @param requestHandlerMapping the default request handler mapping
     */
    public RootCrudHandlerMapping(RequestMappingHandlerMapping requestHandlerMapping) {
        this.requestHandlerMapping = requestHandlerMapping;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
        Object requestMappingHandler = findRequestMappingHandler(request);
        if (requestMappingHandler != null) {
            return requestMappingHandler;
        }
        return findCustomHandler(request);
    }

    private Object findRequestMappingHandler(HttpServletRequest request) throws Exception {
        if (requestHandlerMapping != null) {
            try {
                HandlerExecutionChain result = requestHandlerMapping.getHandler(request);
                if (result != null) {
                    return result.getHandler();
                }
            } catch (HttpRequestMethodNotSupportedException hrmnse) {
                // Whenever the request method is not supported, return our convention
            }
        }
        return null;
    }
    
    private Object findCustomHandler(HttpServletRequest request) throws Exception {
        String basePath = UrlUtils.getRootPath(request);
        PublicHandlerMapping delegateHandler = handlerMappings.get(basePath);
        if (delegateHandler != null) {
            return delegateHandler.getHandlerInternal(request);
        }
        return null;
    }

    /**
     * Register a custom handler mapping.
     * 
     * @param basePath the base path
     * @param handlerMapping the handler mapping
     */
    public void register(String basePath, PublicHandlerMapping handlerMapping) {
        handlerMappings.put(basePath, handlerMapping);
    }

}
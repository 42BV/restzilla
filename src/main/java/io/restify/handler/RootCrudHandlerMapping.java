package io.restify.handler;

import io.restify.UrlUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.PriorityOrdered;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
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
    
    /**
     * Handlers mapped per entity type.
     */
    private final Map<String, PublicHandlerMapping> handlerMappings = new HashMap<String, PublicHandlerMapping>();
    
    /**
     * Exceptions that should be skipped.
     */
    private final Set<Class<?>> skippedExceptions = new HashSet<Class<?>>();

    /**
     * Custom handlers for @RequestMapping methods.
     */
    private final RequestMappingHandlerMapping requestHandlerMapping;

    /**
     * Create a new handler mapping.
     * 
     * @param requestHandlerMapping
     *            the {@link RequestMappingHandlerMapping} handler mapping
     */
    public RootCrudHandlerMapping(RequestMappingHandlerMapping requestHandlerMapping) {
        this.requestHandlerMapping = requestHandlerMapping;
        
        // Register the exceptions from our request handler to skip
        this.skippedExceptions.add(HttpRequestMethodNotSupportedException.class);
        this.skippedExceptions.add(UnsatisfiedServletRequestParameterException.class);
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
        return findCrudHandler(request); // When no custom mapping exists
    }

    private Object findRequestMappingHandler(HttpServletRequest request) throws Exception {
        if (requestHandlerMapping != null) {
            try {
                HandlerExecutionChain result = requestHandlerMapping.getHandler(request);
                if (result != null) {
                    return result.getHandler();
                }
            } catch (Exception exception) {
                // Break the exception when allowed, delegating the request handling to our defaults
                if (!skippedExceptions.contains(exception.getClass())) {
                    throw exception;
                }
            }
        }
        return null;
    }
    
    private Object findCrudHandler(HttpServletRequest request) throws Exception {
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
    public void registerHandler(String basePath, PublicHandlerMapping handlerMapping) {
        handlerMappings.put(basePath, handlerMapping);
    }
    
    /**
     * Register an exception that should be skipped.
     * 
     * @param exceptionClass the exception class
     */
    public void registerSkippedException(Class<?> exceptionClass) {
        skippedExceptions.add(exceptionClass);
    }

}
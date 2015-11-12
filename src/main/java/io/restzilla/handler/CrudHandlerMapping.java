package io.restzilla.handler;

import io.restzilla.util.UrlUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
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
public class CrudHandlerMapping extends AbstractHandlerMapping implements PriorityOrdered {
    
    /**
     * Handlers mapped per entity type.
     */
    private final Map<String, EntityHandlerMapping> handlerMappings = new HashMap<String, EntityHandlerMapping>();
    
    /**
     * Exceptions that should be skipped.
     */
    private final Set<Class<?>> skippedExceptions = new HashSet<Class<?>>();

    /**
     * The application context.
     */
    private final ApplicationContext applicationContext;

    /**
     * Determines if this bean is fully initialized yet.
     */
    private AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Delegate request handler mapping.
     */
    private RequestMappingHandlerMapping requestHandlerMapping;

    /**
     * Create a new handler mapping.
     * 
     * @param applicationContext
     *            the initialized {@link ApplicationContext}
     */
    public CrudHandlerMapping(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        // Register the exceptions from our request handler to skip
        this.skippedExceptions.add(HttpRequestMethodNotSupportedException.class);
        this.skippedExceptions.add(UnsatisfiedServletRequestParameterException.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
        if (!initialized.getAndSet(true)) {
            init();
        }
        Object requestMappingHandler = findRequestMappingHandler(request);
        if (requestMappingHandler != null) {
            return requestMappingHandler;
        }
        return findCrudHandler(request); // When no custom mapping exists
    }

    private void init() {
        String[] beanNames = applicationContext.getBeanNamesForType(RequestMappingHandlerMapping.class);
        if (beanNames.length > 0) {
            requestHandlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        }
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
        EntityHandlerMapping delegateHandler = handlerMappings.get(basePath);
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
    public void registerHandler(String basePath, EntityHandlerMapping handlerMapping) {
        handlerMappings.put(UrlUtils.stripSlashes(basePath), handlerMapping);
    }
    
    /**
     * Register an exception that should be skipped.
     * 
     * @param exceptionClass the exception class
     */
    public void registerSkippedException(Class<?> exceptionClass) {
        skippedExceptions.add(exceptionClass);
    }
    
    /**
     * Retrieve all handler mappings.
     * 
     * @return the handler mappings
     */
    public Collection<EntityHandlerMapping> getHandlerMappings() {
        return handlerMappings.values();
    }

}
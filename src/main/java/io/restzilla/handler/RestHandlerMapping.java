package io.restzilla.handler;

import io.restzilla.RestInformation;
import io.restzilla.util.UrlUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class RestHandlerMapping extends AbstractHandlerMapping implements PriorityOrdered {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RestHandlerMapping.class);

    /**
     * Handlers mapped per entity type.
     */
    private final Map<String, ResourceHandlerMapping> handlerMappings = new HashMap<String, ResourceHandlerMapping>();
    
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
    public RestHandlerMapping(ApplicationContext applicationContext) {
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
        String basePath = UrlUtils.getBasePath(request);
        ResourceHandlerMapping delegateHandler = handlerMappings.get(basePath.toUpperCase());
        if (delegateHandler != null) {
            return delegateHandler.getHandlerInternal(request);
        }
        return null;
    }


    /**
     * Register a custom handler mapping.
     * 
     * @param handlerMapping the handler mapping
     */
    public void registerHandlerMapping(ResourceHandlerMapping handlerMapping) {
        RestInformation information = handlerMapping.getInformation();
        String basePath = information.getBasePath();
        if (basePath.contains(UrlUtils.SLASH)) {
            LOGGER.warn("Overlooked REST resource /{}, because multiple slashes are not supported.", basePath);
        } else if (handlerMappings.containsKey(basePath.toUpperCase())) {
            LOGGER.warn("Duplicated REST resource /{}", basePath);
        } else {
            handlerMappings.put(basePath.toUpperCase(), handlerMapping);
            handlerMapping.describe(LOGGER);
        }
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
    public Collection<ResourceHandlerMapping> getHandlerMappings() {
        return handlerMappings.values();
    }

}
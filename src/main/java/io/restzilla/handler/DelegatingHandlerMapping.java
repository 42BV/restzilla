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
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Root handler mapping for managing CRUD requests.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class DelegatingHandlerMapping extends AbstractHandlerMapping implements PriorityOrdered {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatingHandlerMapping.class);

    /**
     * Handlers mapped per entity type.
     */
    private final Map<String, ResourceHandlerMapping> handlerMappings = new HashMap<String, ResourceHandlerMapping>();
    
    /**
     * Exceptions that should be skipped.
     */
    private final Set<Class<?>> skippedExceptions = new HashSet<Class<?>>();

    /**
     * Default request handler mapping, can be lazily initialized.
     */
    private HandlerMapping defaultHandlerMapping;
    
    /**
     * Determines if this bean is fully initialized yet.
     */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * The application context.
     */
    private final ApplicationContext applicationContext;

    {
        this.skippedExceptions.add(HttpRequestMethodNotSupportedException.class);
        this.skippedExceptions.add(UnsatisfiedServletRequestParameterException.class);
    }

    /**
     * Create a new handler mapping.
     * 
     * @param defaultHandlerMapping
     *            the default {@link HandlerMapping}
     */
    public DelegatingHandlerMapping(HandlerMapping defaultHandlerMapping) {
        this.defaultHandlerMapping = defaultHandlerMapping;
        this.applicationContext = null;
    }
    
    /**
     * Create a new handler mapping.
     * 
     * @param applicationContext
     *            the initialized {@link ApplicationContext}
     */
    public DelegatingHandlerMapping(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
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
        return findDelegateHandler(request); // When no custom mapping exists
    }

    private void init() {
        // Lazily initialize the default handler mapping when not provided
        if (defaultHandlerMapping == null) {
            String[] beanNames = applicationContext.getBeanNamesForType(RequestMappingHandlerMapping.class);
            if (beanNames.length > 0) {
                defaultHandlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
            }
        }
    }

    private Object findRequestMappingHandler(HttpServletRequest request) throws Exception {
        if (defaultHandlerMapping != null) {
            try {
                HandlerExecutionChain result = defaultHandlerMapping.getHandler(request);
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
    
    private Object findDelegateHandler(HttpServletRequest request) throws Exception {
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
    public void registerCustomHandler(ResourceHandlerMapping handlerMapping) {
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
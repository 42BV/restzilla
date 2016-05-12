package io.restzilla.test;

import io.restzilla.RestInformation;
import io.restzilla.handler.ResourceHandlerMapping;
import io.restzilla.handler.ResourceHandlerMappingFactory;
import io.restzilla.handler.RestHandlerMapping;

import java.lang.reflect.Method;

import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerMapping;

/**
 * 
 *
 * @author jeroen
 * @since May 12, 2016
 */
public class RestStandaloneMockMvcBuilder extends StandaloneMockMvcBuilder {
    
    private static final String HANDLER_MAPPING_BEAN_NAME = "requestMappingHandlerMapping";
    private static final String ADD_BEAN_METHOD_NAME = "addBean";

    private final ResourceHandlerMappingFactory handlerMappingFactory;
    private final Object[] controllers;
    
    public RestStandaloneMockMvcBuilder(ResourceHandlerMappingFactory handlerMappingFactory, Object... controllers) {
        super(controllers);
        this.handlerMappingFactory = handlerMappingFactory;
        this.controllers = controllers;
    }

    /* (non-Javadoc)
     * @see org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder#initWebAppContext()
     */
    @Override
    protected WebApplicationContext initWebAppContext() {
        WebApplicationContext wac = super.initWebAppContext();

        HandlerMapping defaultHandlerMapping = wac.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
        RestHandlerMapping restHandlerMapping = buildRestHandlerMapping(defaultHandlerMapping);

        Method method = ReflectionUtils.findMethod(wac.getClass(), ADD_BEAN_METHOD_NAME, String.class, Object.class);
        ReflectionUtils.makeAccessible(method);
        ReflectionUtils.invokeMethod(method, wac, HANDLER_MAPPING_BEAN_NAME, restHandlerMapping);

        return wac;
    }

    protected RestHandlerMapping buildRestHandlerMapping(HandlerMapping defaultHandlerMapping) {
        RestHandlerMapping handlerMapping = new RestHandlerMapping(defaultHandlerMapping);
        for (Object controller : controllers) {
            Class<?> controllerClass = controller.getClass();
            if (RestInformation.isSupported(controllerClass)) {
                RestInformation information = new RestInformation(controller.getClass());
                ResourceHandlerMapping resourceHandler = handlerMappingFactory.build(information);
                handlerMapping.registerCustomHandler(resourceHandler);
            }
        }
        return handlerMapping;
    }

}

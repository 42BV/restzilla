package io.restzilla.test;

import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.web.context.WebApplicationContext;

/**
 * 
 *
 * @author jeroen
 * @since May 12, 2016
 */
public class RestStandaloneMockMvcBuilder extends StandaloneMockMvcBuilder {
    
    /* (non-Javadoc)
     * @see org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder#initWebAppContext()
     */
    @Override
    protected WebApplicationContext initWebAppContext() {
        WebApplicationContext wac = super.initWebAppContext();
        return wac;
    }

}

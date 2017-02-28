package io.restzilla;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Template for testing the dynamic REST endpoints.
 *
 * @author Jeroen van Schagen
 * @since Aug 24, 2015
 */
public abstract class AbstractControllerTest extends AbstractSpringTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    protected MockMvc webClient;

    @Before
    public void initWebClient() {
        DefaultMockMvcBuilder webClientBuilder = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .alwaysDo(log());
        
        prepareWebClient(webClientBuilder);
        this.webClient = webClientBuilder.build();
    }

    protected void prepareWebClient(DefaultMockMvcBuilder webClientBuilder) {
    }

}

package io.restzilla;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Template for testing the dynamic REST endpoints.
 *
 * @author Jeroen van Schagen
 * @since Aug 24, 2015
 */
public abstract class AbstractControllerTest extends AbstractSpringTest {
    
    @Autowired
    private RequestMappingHandlerAdapter handlerAdapter;
    
    @Autowired
    private HandlerMapping handlerMapping;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    protected MockHttpServletResponse call(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        handlerAdapter.handle(request, response, getHandlerChain(request).getHandler());
        return response;
    }
    
    protected HandlerExecutionChain getHandlerChain(MockHttpServletRequest request) throws Exception {
        return handlerMapping.getHandler(request);
    }

    protected void setContentAsJson(MockHttpServletRequest request, Object value) throws Exception {
        String json = objectMapper.writeValueAsString(value);
        setValueAsJson(request, (String) json);
    }
    
    protected void setValueAsJson(MockHttpServletRequest request, String json) throws Exception {
        request.setContentType("application/json");
        request.setContent(json.getBytes());
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

}

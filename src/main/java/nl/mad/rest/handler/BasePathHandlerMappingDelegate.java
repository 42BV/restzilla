package nl.mad.rest.handler;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

/**
 * Handler mapping that delegates based on the first path element.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class BasePathHandlerMappingDelegate extends AbstractHandlerMapping {
    
    private Map<String, CrudHandlerMapping> handlerMappings = new HashMap<String, CrudHandlerMapping>();

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
        String basePath = StringUtils.substringBetween(request.getRequestURI() + "/", "/", "/");
        CrudHandlerMapping handlerMapping = handlerMappings.get(basePath);
        if (handlerMapping != null) {
            return handlerMapping.getHandlerInternal(request);
        }
        return handlerMapping;
    }
    
    public void register(String basePath, CrudHandlerMapping handlerMapping) throws Exception {
        handlerMappings.put(basePath, handlerMapping);
    }

}
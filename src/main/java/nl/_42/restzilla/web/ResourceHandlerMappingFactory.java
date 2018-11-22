package nl._42.restzilla.web;

/**
 * Responsible for building the rest handlers.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public interface ResourceHandlerMappingFactory {
    
    /**
     * Build a new handler mapping.
     * 
     * @param resourceType the resource type
     * @return the handler mapping
     */
    ResourceHandlerMapping build(Class<?> resourceType);
    
}

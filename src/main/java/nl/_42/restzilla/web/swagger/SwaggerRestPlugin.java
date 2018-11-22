/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.web.swagger;

import nl._42.restzilla.web.RestInformation;
import nl._42.restzilla.web.ResourceHandlerMapping;
import nl._42.restzilla.web.RestHandlerMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.CaseFormat;
import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.core.SwaggerCache;
import com.mangofactory.swagger.models.dto.ApiListing;
import com.mangofactory.swagger.models.dto.ApiListingReference;
import com.mangofactory.swagger.models.dto.ResourceListing;
import com.mangofactory.swagger.models.dto.builder.ApiListingBuilder;
import com.mangofactory.swagger.paths.SwaggerPathProvider;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;

/**
 * Exposes the default REST mappings to Swagger, in addition
 * to the regular Spring MVC mappings.
 *
 * @author Jeroen van Schagen
 * @since Sep 3, 2015
 */
public class SwaggerRestPlugin extends SwaggerSpringMvcPlugin {

    private final SpringSwaggerConfig springSwaggerConfig;

    private final RestHandlerMapping crudHandlerMapping;

    private final SwaggerApiDescriptor apiDescriptor;

    private SwaggerPathProvider swaggerPathProvider;
    
    private String swaggerGroup = "default";

    public SwaggerRestPlugin(
      final SpringSwaggerConfig springSwaggerConfig,
      final RestHandlerMapping crudHandlerMapping
    ) {

        this(springSwaggerConfig, crudHandlerMapping, new DefaultApiDescriptor());
    }

    public SwaggerRestPlugin(
      final SpringSwaggerConfig springSwaggerConfig,
      final RestHandlerMapping crudHandlerMapping,
      final SwaggerApiDescriptor apiDescriptor
    ) {

        super(springSwaggerConfig);

        this.springSwaggerConfig = springSwaggerConfig;
        this.crudHandlerMapping = crudHandlerMapping;
        this.apiDescriptor = apiDescriptor;
        this.swaggerPathProvider = springSwaggerConfig.defaultSwaggerPathProvider();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SwaggerSpringMvcPlugin swaggerGroup(String swaggerGroup) {
        this.swaggerGroup = swaggerGroup;
        return super.swaggerGroup(swaggerGroup);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SwaggerSpringMvcPlugin pathProvider(SwaggerPathProvider swaggerPathProvider) {
        this.swaggerPathProvider = swaggerPathProvider;
        return super.pathProvider(swaggerPathProvider);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        super.initialize();

        SwaggerCache swaggerCache = springSwaggerConfig.swaggerCache();
        for (ResourceHandlerMapping handlerMapping : crudHandlerMapping.getHandlerMappings()) {
            String resourceName = addResourceListings(swaggerCache, handlerMapping.getInformation());
            addApiListings(swaggerCache, resourceName, handlerMapping);
        }
    }
    
    private String addResourceListings(SwaggerCache swaggerCache, RestInformation information) {
        final String resourceName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, information.getEntityClass().getSimpleName() + "Controller");
        final String path = "/" + swaggerGroup + "/" + resourceName;
        
        ResourceListing resourceListing = swaggerCache.getResourceListing(swaggerGroup);
        if (!hasApiListingReference(resourceListing, path)) {
            resourceListing.getApis().add(new ApiListingReference(path, resourceName, 0));
        }

        return resourceName;
    }

    private boolean hasApiListingReference(ResourceListing resourceListing, String path) {
        for (ApiListingReference reference : resourceListing.getApis()) {
            if (path.equals(reference.getPath())) {
                return true;
            }
        }
        return false;
    }

    private void addApiListings(SwaggerCache swaggerCache, String resourceName, ResourceHandlerMapping handlerMapping) {
        RestInformation information = handlerMapping.getInformation();
        ApiListing apiListing = getApiListing(swaggerCache, resourceName, information);
        apiDescriptor.enhance(information, apiListing, springSwaggerConfig.defaultModelProvider());
    }

    private ApiListing getApiListing(SwaggerCache swaggerCache, String resourceName, RestInformation information) {
        Map<String, ApiListing> apiListings = swaggerCache.getSwaggerApiListingMap().get(swaggerGroup);
        ApiListing apiListing = apiListings.get(resourceName);
        if (apiListing == null) {
            apiListing = buildApiListing(information);
            apiListings.put(resourceName, apiListing);
        }
        return apiListing;
    }

    private ApiListing buildApiListing(RestInformation information) {
        return new ApiListingBuilder()
                    .basePath(swaggerPathProvider.getApplicationBasePath())
                    .description(information.getEntityClass().getSimpleName())
                    .apis(new ArrayList<>())
                    .models(new HashMap<>())
                    .consumes(Arrays.asList("*/*"))
                    .produces(Arrays.asList("*/*"))
                    .build();
    }

}

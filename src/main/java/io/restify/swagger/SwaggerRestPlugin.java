/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.swagger;

import io.restify.EntityInformation;
import io.restify.handler.CrudHandlerMapping;
import io.restify.handler.EntityHandlerMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import com.google.common.base.CaseFormat;
import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.core.SwaggerCache;
import com.mangofactory.swagger.models.dto.ApiDescription;
import com.mangofactory.swagger.models.dto.ApiListing;
import com.mangofactory.swagger.models.dto.ApiListingReference;
import com.mangofactory.swagger.models.dto.ResourceListing;
import com.mangofactory.swagger.models.dto.builder.ApiListingBuilder;
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
    
    private final CrudHandlerMapping crudHandlerMapping;
    
    private String swaggerGroup = "default";

    public SwaggerRestPlugin(SpringSwaggerConfig springSwaggerConfig, CrudHandlerMapping crudHandlerMapping) {
        super(springSwaggerConfig);
        this.springSwaggerConfig = springSwaggerConfig;
        this.crudHandlerMapping = crudHandlerMapping;
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
    protected void initialize() {
        super.initialize();

        SwaggerCache swaggerCache = springSwaggerConfig.swaggerCache();
        for (EntityHandlerMapping handlerMapping : crudHandlerMapping.getHandlerMappings()) {
            String resourceName = addResourceListings(swaggerCache, handlerMapping.getInformation());
            addApiListings(swaggerCache, resourceName, handlerMapping);
        }
    }
    
    private String addResourceListings(SwaggerCache swaggerCache, EntityInformation information) {
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

    private void addApiListings(SwaggerCache swaggerCache, String resourceName, EntityHandlerMapping handlerMapping) {
        ApiListing apiListing = getApiListing(swaggerCache, resourceName, handlerMapping.getInformation());
        if (handlerMapping instanceof SwaggerApiDescriptor) {
            ((SwaggerApiDescriptor) handlerMapping).addApiDescriptions(apiListing);
        }
    }

    private ApiListing getApiListing(SwaggerCache swaggerCache, String resourceName, EntityInformation information) {
        Map<String, ApiListing> apiListings = swaggerCache.getSwaggerApiListingMap().get(swaggerGroup);
        ApiListing apiListing = apiListings.get(resourceName);
        if (apiListing == null) {
            apiListing = buildApiListing(resourceName, information);
            apiListings.put(resourceName, apiListing);
        }
        return apiListing;
    }

    private ApiListing buildApiListing(String resourceName, EntityInformation information) {
        return new ApiListingBuilder()
                    .basePath(information.getBasePath())
                    .description(information.getEntityClass().getSimpleName())
                    .apis(new ArrayList<ApiDescription>())
                    .consumes(Arrays.asList("*/*"))
                    .produces(Arrays.asList("*/*"))
                    .build();
    }

}

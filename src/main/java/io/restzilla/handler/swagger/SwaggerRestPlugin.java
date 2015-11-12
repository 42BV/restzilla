/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.handler.swagger;

import io.restzilla.RestInformation;
import io.restzilla.handler.RestHandlerMapping;
import io.restzilla.handler.EntityHandlerMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.CaseFormat;
import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.core.SwaggerCache;
import com.mangofactory.swagger.models.dto.ApiDescription;
import com.mangofactory.swagger.models.dto.ApiListing;
import com.mangofactory.swagger.models.dto.ApiListingReference;
import com.mangofactory.swagger.models.dto.Model;
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
    
    private SwaggerPathProvider swaggerPathProvider;
    
    private String swaggerGroup = "default";

    public SwaggerRestPlugin(SpringSwaggerConfig springSwaggerConfig, RestHandlerMapping crudHandlerMapping) {
        super(springSwaggerConfig);
        this.springSwaggerConfig = springSwaggerConfig;
        this.crudHandlerMapping = crudHandlerMapping;
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
        for (EntityHandlerMapping handlerMapping : crudHandlerMapping.getHandlerMappings()) {
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

    private void addApiListings(SwaggerCache swaggerCache, String resourceName, EntityHandlerMapping handlerMapping) {
        ApiListing apiListing = getApiListing(swaggerCache, resourceName, handlerMapping.getInformation());
        if (handlerMapping instanceof SwaggerApiDescriptor) {
            ((SwaggerApiDescriptor) handlerMapping).enhance(apiListing, springSwaggerConfig.defaultModelProvider());
        }
    }

    private ApiListing getApiListing(SwaggerCache swaggerCache, String resourceName, RestInformation information) {
        Map<String, ApiListing> apiListings = swaggerCache.getSwaggerApiListingMap().get(swaggerGroup);
        ApiListing apiListing = apiListings.get(resourceName);
        if (apiListing == null) {
            apiListing = buildApiListing(resourceName, information);
            apiListings.put(resourceName, apiListing);
        }
        return apiListing;
    }

    private ApiListing buildApiListing(String resourceName, RestInformation information) {
        return new ApiListingBuilder()
                    .basePath(swaggerPathProvider.getApplicationBasePath())
                    .description(information.getEntityClass().getSimpleName())
                    .apis(new ArrayList<ApiDescription>())
                    .models(new HashMap<String, Model>())
                    .consumes(Arrays.asList("*/*"))
                    .produces(Arrays.asList("*/*"))
                    .build();
    }

}

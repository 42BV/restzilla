/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.swagger;

import io.restify.EntityInformation;
import io.restify.handler.CrudHandlerMapping;
import io.restify.handler.EntityHandlerMapping;

import java.util.HashMap;
import java.util.Map;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.core.SwaggerCache;
import com.mangofactory.swagger.models.dto.ApiListing;
import com.mangofactory.swagger.models.dto.ApiListingReference;
import com.mangofactory.swagger.models.dto.Model;
import com.mangofactory.swagger.models.dto.ModelProperty;
import com.mangofactory.swagger.models.dto.ResourceListing;
import com.mangofactory.swagger.models.dto.builder.ApiListingBuilder;
import com.mangofactory.swagger.models.dto.builder.ModelBuilder;
import com.mangofactory.swagger.models.dto.builder.ModelPropertyBuilder;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;

/**
 * Configures the default REST mappings to Swagger.
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
        final String resourceName = information.getEntityClass().getSimpleName() + "Controller";
        final String path = "/" + swaggerGroup + "/" + resourceName;
        
        ResourceListing resourceListing = swaggerCache.getResourceListing(swaggerGroup);
        for (ApiListingReference reference : resourceListing.getApis()) {
            if (path.equals(reference.getPath())) {
                return resourceName; // Resource already exists
            }
        }

        ApiListingReference reference = new ApiListingReference(path, resourceName, 0);
        resourceListing.getApis().add(reference);
        
        return resourceName;
    }

    private void addApiListings(SwaggerCache swaggerCache, String resourceName, EntityHandlerMapping handlerMapping) {
        Map<String, ApiListing> apiListings = swaggerCache.getSwaggerApiListingMap().get(swaggerGroup);
        ApiListing apiListing = apiListings.get(resourceName);
        if (apiListing == null) {
            apiListings.put("mylisting", buildListing());
        }
    }

    private ApiListing buildListing() {
        Map<String, Model> models = new HashMap<String, Model>();
        models.put("mymodel", buildModel());
        
        return new ApiListingBuilder()
                    .basePath("/mypath")
                    .description("Some description")
                    .resourcePath("/myresourcepath")
                    .models(models)
                    .build();
    }
    
    private Model buildModel() {
        Map<String, ModelProperty> properties = new HashMap<String, ModelProperty>();
        properties.put("myproperty", buildModelProperty());
        
        return new ModelBuilder()
                    .name("myname")
                    .qualifiedType("MyQualifiedType")
                    .properties(properties)
                    .build(); 
    }
    
    private ModelProperty buildModelProperty() {
        return new ModelPropertyBuilder()
                    .type("mytype")
                    .qualifiedType("MyQualifiedType")
                    .build();
    }

}

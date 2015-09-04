/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.swagger;

import io.restify.util.UrlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.mangofactory.swagger.models.ModelContext;
import com.mangofactory.swagger.models.ModelProvider;
import com.mangofactory.swagger.models.dto.ApiDescription;
import com.mangofactory.swagger.models.dto.ApiListing;
import com.mangofactory.swagger.models.dto.Authorization;
import com.mangofactory.swagger.models.dto.Model;
import com.mangofactory.swagger.models.dto.Operation;
import com.mangofactory.swagger.models.dto.Parameter;
import com.mangofactory.swagger.models.dto.builder.ApiDescriptionBuilder;
import com.mangofactory.swagger.models.dto.builder.OperationBuilder;
import com.mangofactory.swagger.models.dto.builder.ParameterBuilder;

/**
 * Utility for creating swagger mappings.
 *
 * @author Jeroen van Schagen
 * @since Sep 4, 2015
 */
public class SwaggerUtils {
    
    // Models
    
    /**
     * Add a model to the listings, whenever a similar model
     * does not exist yet.
     * 
     * @param listing the API listing
     * @param modelClass the model class
     * @param modelProvider the model provider
     */
    public static void addIfNotExists(ApiListing listing, Class<?> modelClass, ModelProvider modelProvider) {
        final String name = modelClass.getSimpleName();
        if (!listing.getModels().containsKey(name)) {
            ModelContext context = ModelContext.inputParam(modelClass);
            Optional<Model> model = modelProvider.modelFor(context);
            if (model.isPresent()) {
                listing.getModels().put(name, model.get());
            }
        }
    }
    
    // API descriptions

    /**
     * Add an API description to the listings, whenever a similar
     * mapping does not exist yet.
     * 
     * @param listing the API listing
     * @param description the description
     */
    public static void addIfNotExists(ApiListing listing, ApiDescription description) {
        if (!hasDescription(listing, description)) {
            listing.getApis().add(description);
        }
    }
    
    private static boolean hasDescription(ApiListing listing, ApiDescription description) {
        for (ApiDescription other : listing.getApis()) {
            if (UrlUtils.isSamePath(other.getPath(), description.getPath())) {
                for (Operation operation : description.getOperations()) {
                    if (hasOperation(other.getOperations(), operation)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean hasOperation(List<Operation> operations, Operation operation) {
        for (Operation other : operations) {
            if (other.getMethod().equals(operation.getMethod()) && hasSameParameters(other, operation)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean hasSameParameters(Operation left, Operation right) {
        return getRequiredParameterNames(left).equals(getRequiredParameterNames(right));
    }
    
    private static Set<String> getRequiredParameterNames(Operation operation) {
        Set<String> parameterNames = new HashSet<String>();
        for (Parameter parameter : operation.getParameters()) {
            if (parameter.isRequired()) {
                parameterNames.add(parameter.getName());
            }
        }
        return parameterNames;
    }
    
    // Builders
    
    /**
     * Build a new API description.
     * 
     * @param path the path
     * @param description the description
     * @param operation the operation
     */
    public static ApiDescription buildDescription(String path, String description, Operation operation) {
        return new ApiDescriptionBuilder()
                      .path(path)
                      .description(description)
                      .operations(Arrays.asList(operation))
                      .build();
    }
    
    /**
     * Start building a new operation, all values are given an
     * expected value to make the configuration as minimal as possible.
     * 
     * @param name the operation name
     */
    public static OperationBuilder newOperation(String name) {
        return new OperationBuilder()
                      .authorizations(new ArrayList<Authorization>())
                      .produces(Arrays.asList("*/*"))
                      .consumes(Arrays.asList("application/json"))
                      .parameters(new ArrayList<Parameter>())
                      .nickname(name)
                      .notes(name)
                      .summary(name)
                      .deprecated("false");
    }

    /**
     * Build a path parameter.
     * 
     * @param name the path name
     * @param type the parameter type
     */
    public static Parameter newPathParameter(String name, Class<?> type) {
        return new ParameterBuilder()
                      .dataType(type.getSimpleName().toLowerCase())
                      .name(name)
                      .parameterType("path")
                      .build();
    }

    /**
     * Build a body parameter.
     * 
     * @param type the body type
     */
    public static Parameter newBodyParameter(Class<?> type) {
        return new ParameterBuilder()
                      .dataType(type.getSimpleName())
                      .name("body")
                      .parameterType("body")
                      .build();
    }
    
}

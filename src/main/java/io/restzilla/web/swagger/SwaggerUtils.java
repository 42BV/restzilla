/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.web.swagger;

import io.restzilla.util.UrlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.Objects;
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

    /**
     * Start building a new API description.
     *
     * @param name the name of the API call
     * @param path the path to call
     * @param method the request method
     * @return the description builder
     */
    public static DescriptionBuilder newDescription(String name, String path, RequestMethod method) {
        return new DescriptionBuilder(name, path, method);
    }
    
    /**
     * Builder that creates an API description. This is an enhanced
     * wrapper over the default Swagger builders.
     */
    public static class DescriptionBuilder {

        private final ApiDescriptionBuilder descriptionBuilder;
        
        private final OperationBuilder operationBuilder;
        
        private final List<Parameter> parameters = new ArrayList<Parameter>();

        private DescriptionBuilder(String description, String path, RequestMethod method) {
            descriptionBuilder = new ApiDescriptionBuilder().description(description).path(path);
            operationBuilder = newOperation(description).method(method.name());
        }
        
        public DescriptionBuilder noResponseClass() {
            operationBuilder.responseClass(Void.class.getSimpleName().toLowerCase());
            return this;
        }

        public DescriptionBuilder responseClass(Class<?> responseClass) {
            if (Void.class.equals(responseClass)) {
                return noResponseClass();
            }
            operationBuilder.responseClass(responseClass.getSimpleName());
            return this;
        }
        
        public DescriptionBuilder responseClassIterable(Class<?> responseClass) {
            operationBuilder.responseClass("Iterable«" + responseClass.getSimpleName() + "»");
            return this;
        }
        
        public DescriptionBuilder addQueryParameter(String name, Class<?> parameterType, boolean required) {
            parameters.add(newQueryParameter(name, parameterType, required));
            return this;
        }

        public DescriptionBuilder addPathParameter(String name, Class<?> parameterType) {
            parameters.add(newPathParameter(name, parameterType));
            return this;
        }
        
        public DescriptionBuilder addBodyParameter(Class<?> parameterType) {
            parameters.add(newBodyParameter(parameterType));
            return this;
        }

        private ApiDescription build() {
            operationBuilder.parameters(parameters);
            descriptionBuilder.operations(Arrays.asList(operationBuilder.build()));
            return descriptionBuilder.build();
        }
        
        public void register(ApiListing listing) {
            addIfNotExists(listing, build());
        }
        
    }

    private static OperationBuilder newOperation(String name) {
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
    
    private static Parameter newQueryParameter(String name, Class<?> parameterType, boolean required) {
        return new ParameterBuilder().dataType(parameterType.getSimpleName().toLowerCase()).name(name).parameterType("query").required(required).build();
    }

    private static Parameter newPathParameter(String name, Class<?> parameterType) {
        return new ParameterBuilder()
                      .dataType(parameterType.getSimpleName().toLowerCase())
                      .name(name)
                      .parameterType("path")
                      .build();
    }

    private static Parameter newBodyParameter(Class<?> parameterType) {
        return new ParameterBuilder()
                      .dataType(parameterType.getSimpleName())
                      .name("body")
                      .parameterType("body")
                      .build();
    }

    // Registration
    
    /**
     * Add an API description to the listings, whenever a similar
     * mapping does not exist yet.
     * 
     * @param listing the API listing
     * @param description the description
     */
    private static void addIfNotExists(ApiListing listing, ApiDescription description) {
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
            if (parameter != null) {
                if (Objects.equal(Boolean.TRUE, parameter.isRequired())) {
                    parameterNames.add(parameter.getName());
                }
            }
        }
        return parameterNames;
    }
    
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
    
}

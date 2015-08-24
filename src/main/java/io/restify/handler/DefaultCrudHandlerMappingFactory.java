/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.handler;

import io.restify.EntityInformation;
import io.restify.service.CrudService;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Default implementation of the {@link CrudHandlerMappingFactory}.
 *
 * @author Jeroen van Schagen
 * @since Aug 21, 2015
 */
public class DefaultCrudHandlerMappingFactory implements CrudHandlerMappingFactory {
    
    private final ObjectMapper objectMapper;
    
    private final ConversionService conversionService;
    
    public DefaultCrudHandlerMappingFactory(ObjectMapper objectMapper, ConversionService conversionService) {
        this.objectMapper = objectMapper;
        this.conversionService = conversionService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultHandlerMapping build(CrudService<?, ?> service, EntityInformation information) {
        return new DefaultHandlerMapping(new CrudController(service, information));
    }

    /**
     * Controller implementation, delegated from handler mapping.
     *
     * @author Jeroen van Schagen
     * @since Aug 21, 2015
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private class CrudController {
        
        private final CrudService service;
        
        private final EntityInformation information;
        
        public CrudController(CrudService service, EntityInformation information) {
            this.service = service;
            this.information = information;
        }
        
        /**
         * Retrieve all entities.
         * 
         * @return the entities, in result type
         */
        @ResponseBody
        public Object findAll() {
            Collection<?> output = service.findAll();
            return convertAll(output, information.getResultType());
        }
        
        private Collection<Object> convertAll(Collection<?> inputs, Class<?> resultType) {
            Collection<Object> converted = new ArrayList<Object>();
            for (Object input : inputs) {
                converted.add(conversionService.convert(input, resultType));
            }
            return converted;
        }
        
        /**
         * Retrieve an entity.
         * 
         * @param id the identifier
         * @return the entity, in result type
         */
        @ResponseBody
        public Object findOne(HttpServletRequest request) {
            Serializable id = extractId(request);
            Object output = service.findOne(id);
            return conversionService.convert(output, information.getResultType());
        }
        
        private Serializable extractId(HttpServletRequest request) {
            String raw = StringUtils.substringAfterLast(request.getRequestURI(), "/");
            return (Serializable) conversionService.convert(raw, information.getIdentifierClass());
        }
        
        /**
         * Create a new entity.
         * 
         * @return the entity
         */
        @ResponseBody
        public Object create(HttpServletRequest request) throws Exception {
            Object input = objectMapper.readValue(request.getReader(), information.getCreateType());
            Object entity = conversionService.convert(input, information.getEntityClass());
            Object output = service.save(entity);
            return conversionService.convert(output, information.getResultType());
        }
        
        /**
         * Update an entity.
         * 
         * @return the updated entity
         */
        @ResponseBody
        public Object update(HttpServletRequest request) throws Exception {
            Object input = objectMapper.readValue(request.getReader(), information.getUpdateType());
            Object entity = conversionService.convert(input, information.getEntityClass());
            Object output = service.save(entity);
            return conversionService.convert(output, information.getResultType());
        }
        
        /**
         * Deletes an entity.
         */
        @ResponseBody
        public void delete(HttpServletRequest request) {
            Serializable id = extractId(request);
            service.delete(id);
        }

    }
    
    /**
     * Handler mapping that delegates to the underlying controller.
     */
    private class DefaultHandlerMapping extends PublicHandlerMapping {
        
        private final CrudController controller;
        
        public DefaultHandlerMapping(CrudController controller) {
            this.controller = controller;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Object getHandlerInternal(HttpServletRequest request) throws Exception {
            Object result = null;
            Method method = findMethod(request);
            if (method != null) {
                result = new HandlerMethod(controller, method);
            }
            return result;
        }
        
        /**
         * Dynamically match our servlet request to one of the service
         * methods. Whenever a request is recognized we return that method.
         * 
         * @param request the incomming request
         * @return the matching method, if any
         */
        private Method findMethod(HttpServletRequest request) throws NoSuchMethodException {
            Method method = null;
            
            int fragments = request.getRequestURI().split("/").length;
            if (fragments == 2) {
                // Request URI only consists of base path
                if (RequestMethod.GET.name().equals(request.getMethod())) {
                    method = CrudController.class.getMethod("findAll");
                } else if (RequestMethod.POST.name().equals(request.getMethod())) {
                    method = CrudController.class.getMethod("create", HttpServletRequest.class);
                } else if (RequestMethod.PUT.name().equals(request.getMethod())) {
                    method = CrudController.class.getMethod("update", HttpServletRequest.class);
                }
            } else if (fragments == 3) {
                // Request URI has an additional path element (ID)
                if (RequestMethod.GET.name().equals(request.getMethod())) {
                    method = CrudController.class.getMethod("findOne", HttpServletRequest.class);
                } else if (RequestMethod.DELETE.name().equals(request.getMethod())) {
                    method = CrudController.class.getMethod("delete", HttpServletRequest.class);
                }
            }
            
            return method;
        }
        
    }

}

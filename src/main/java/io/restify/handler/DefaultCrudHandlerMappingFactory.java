/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.handler;

import io.beanmapper.BeanMapper;
import io.restify.EntityInformation;
import io.restify.UrlUtils;
import io.restify.service.CrudService;

import java.io.Serializable;
import java.lang.reflect.Method;
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
    
    private final BeanMapper beanMapper;
    
    /**
     * Instantiate a new {@link DefaultCrudHandlerMappingFactory}.
     * 
     * @param objectMapper
     *              the {@link ObjectMapper} for JSON parsing and formatting
     * @param conversionService
     *              the {@link ConversionService} for converting between types
     * @param beanMapper
     *              the {@link BeanMapper} for mapping between beans
     */
    public DefaultCrudHandlerMappingFactory(ObjectMapper objectMapper,
                                       ConversionService conversionService,
                                              BeanMapper beanMapper) {
        this.objectMapper = objectMapper;
        this.conversionService = conversionService;
        this.beanMapper = beanMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultHandlerMapping build(CrudService<?, ?> service, EntityInformation information) {
        return new DefaultHandlerMapping(new DefaultCrudController(service, information));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private class DefaultCrudController {
        
        private final CrudService service;
        
        private final EntityInformation information;
        
        public DefaultCrudController(CrudService service, EntityInformation information) {
            this.service = service;
            this.information = information;
        }
        
        /**
         * Retrieve all entities.
         * @return the entities, in result type
         */
        @ResponseBody
        public Object findAll() {
            Collection<?> entities = service.findAll();
            return beanMapper.map(entities, information.getResultType());
        }

        /**
         * Retrieve a single entity by identifier: /{id}
         * @param id the identifier
         * @return the entity, in result type
         */
        @ResponseBody
        public Object findOne(HttpServletRequest request) {
            Serializable id = extractId(request);
            Object entity = service.findOne(id);
            return beanMapper.map(entity, information.getResultType());
        }
        
        private Serializable extractId(HttpServletRequest request) {
            String path = UrlUtils.getPath(request);
            String raw = StringUtils.substringAfterLast(path, UrlUtils.SLASH);
            return conversionService.convert(raw, information.getIdentifierClass().asSubclass(Serializable.class));
        }
        
        /**
         * Creates a new entity. Any content is retrieved from the request body.
         * @return the entity
         */
        @ResponseBody
        public Object create(HttpServletRequest request) throws Exception {
            Object input = objectMapper.readValue(request.getReader(), information.getCreateType());
            Object entity = beanMapper.map(input, information.getEntityClass());
            Object output = service.save(entity);
            return beanMapper.map(output, information.getResultType());
        }
        
        /**
         * Updates an entity. Any content is retrieved from the request body.
         * @return the updated entity
         */
        @ResponseBody
        public Object update(HttpServletRequest request) throws Exception {
            Serializable id = extractId(request);
            Object entity = service.findOne(id);
            Object input = objectMapper.readValue(request.getReader(), information.getUpdateType());
            entity = beanMapper.map(input, entity);
            Object output = service.save(entity);
            return beanMapper.map(output, information.getResultType());
        }
        
        /**
         * Deletes an entity based on an identifier: /{id}
         */
        @ResponseBody
        public void delete(HttpServletRequest request) {
            Serializable id = extractId(request);
            service.delete(id);
        }

    }
    
    private static class DefaultHandlerMapping extends PublicHandlerMapping {
        
        private final DefaultCrudController controller;
        
        public DefaultHandlerMapping(DefaultCrudController controller) {
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
            
            int fragments = UrlUtils.getPath(request).split(UrlUtils.SLASH).length;
            if (fragments == 2) {
                // Request URI only consists of base path
                if (RequestMethod.GET.name().equals(request.getMethod())) {
                    method = DefaultCrudController.class.getMethod("findAll");
                } else if (RequestMethod.POST.name().equals(request.getMethod())) {
                    method = DefaultCrudController.class.getMethod("create", HttpServletRequest.class);
                }
            } else if (fragments == 3) {
                // Request URI has an additional path element /{id}
                if (RequestMethod.GET.name().equals(request.getMethod())) {
                    method = DefaultCrudController.class.getMethod("findOne", HttpServletRequest.class);
                } else if (RequestMethod.PUT.name().equals(request.getMethod())) {
                    method = DefaultCrudController.class.getMethod("update", HttpServletRequest.class);
                } else if (RequestMethod.DELETE.name().equals(request.getMethod())) {
                    method = DefaultCrudController.class.getMethod("delete", HttpServletRequest.class);
                }
            }
            
            return method;
        }
        
    }

}

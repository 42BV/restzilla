/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service.adapter;

import io.restzilla.RestQuery;
import io.restzilla.service.CrudServiceRegistry;
import io.restzilla.service.Listable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.ReflectionUtils;

import com.google.common.base.Preconditions;

/**
 * Listable that performs a custom method by reflection. 
 *
 * @author Jeroen van Schagen
 * @since Dec 9, 2015
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class RepositoryMethodListable<T> implements Listable<T> {
    
    private final CrudServiceRegistry crudServiceRegistry;
    
    private final ConversionService conversionService;

    private final Class entityClass;
    
    private final RestQuery annotation;
    
    private final Map<String, String[]> parameterValues;

    public RepositoryMethodListable(CrudServiceRegistry crudServiceRegistry, 
                                      ConversionService conversionService, 
                                                  Class entityClass, 
                                              RestQuery annotation,
                                  Map<String, String[]> parameterValues) {
        this.crudServiceRegistry = crudServiceRegistry;
        this.conversionService = conversionService;
        this.entityClass = entityClass;
        this.annotation = annotation;
        this.parameterValues = parameterValues;
    }
    
    private InvokeableMethod findMethod(Class<?> returnType) {
        InvokeableMethod method = findMethod(crudServiceRegistry.getService(entityClass), returnType);
        if (method == null) {
            method = findMethod(crudServiceRegistry.getRepository(entityClass), returnType);
        }
        return Preconditions.checkNotNull(method, "Could not find custom finder method '" + annotation.method() + "' for " + entityClass.getName());
    }
    
    private InvokeableMethod findMethod(Object candidate, Class<?> returnType) {
        Class<?> beanClass = AopUtils.getTargetClass(candidate);
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(beanClass);
        for (Method method : methods) {
            if (method.getName().equals(annotation.method()) && method.getReturnType().equals(returnType)) {
                return new InvokeableMethod(candidate, method);
            }
        }
        return null;
    }

    private <P> Object invoke(InvokeableMethod invokable, Class<P> parameterType, P parameterValue) {
        Object[] args = buildArguments(invokable, parameterType, parameterValue);

        try {
            return invokable.invoke(args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not invoke finder method.", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Could not invoke finder method.", e);
        }
    }

    private <P> Object[] buildArguments(InvokeableMethod invokable, Class<P> parameterType, P parameterValue) {
        Class<?>[] parameterTypes = invokable.method.getParameterTypes();
        List<String> parameterNames = collectParameterNames();
        
        Object[] args = new Object[parameterTypes.length];
        for (int index = 0; index < parameterTypes.length; index++) {
            String currentName = (parameterNames.size() - 1) >= index ? parameterNames.get(index) : "";
            Class<?> currentType = parameterTypes[index];
            Object currentValue = null;
            
            if (StringUtils.isNotBlank(currentName)) {
                currentValue = conversionService.convert(parameterValues.get(currentName), currentType);
            } else if (currentType.equals(parameterType)) {
                currentValue = parameterValue;
            }

            args[index] = currentValue;
        }
        return args;
    }

    private List<String> collectParameterNames() {
        List<String> parameterNames = new ArrayList<String>();
        for (String parameter : annotation.parameters()) {
            if (!parameter.contains("=")) {
                parameterNames.add(parameter);
            }
        }
        return parameterNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> findAll(Sort sort) {
        InvokeableMethod invokable = findMethod(Iterable.class);
        return (List<T>) invoke(invokable, Sort.class, sort);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<T> findAll(Pageable pageable) {
        InvokeableMethod invokable = findMethod(Page.class);
        return (Page<T>) invoke(invokable, Pageable.class, pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getEntityClass() {
        return entityClass;
    }
    
    private static class InvokeableMethod {
        
        private final Object bean;
        
        private final Method method;
        
        public InvokeableMethod(Object bean, Method method) {
            this.bean = bean;
            this.method = method;
        }
        
        public Object invoke(Object... args) throws IllegalAccessException, InvocationTargetException {
            return method.invoke(bean, args);
        }
        
    }

}

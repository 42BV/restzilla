package io.restzilla.handler.query;

import io.restzilla.RestInformation;
import io.restzilla.RestInformation.QueryInformation;
import io.restzilla.service.CrudServiceRegistry;
import io.restzilla.service.Listable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.ReflectionUtils;

import com.google.common.base.Preconditions;

/**
 * Detects the query method and invokes it dynamically.
 *
 * @author Jeroen van Schagen
 * @since Dec 9, 2015
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class RepositoryMethodListable<T> implements Listable<T>, Finder<T> {
    
    private final CrudServiceRegistry crudServiceRegistry;
    
    private final ConversionService conversionService;

    private final RestInformation entityInfo;
    
    private final QueryInformation queryInfo;
    
    private final Map<String, String[]> parameterValues;

    public RepositoryMethodListable(CrudServiceRegistry crudServiceRegistry, 
                                      ConversionService conversionService, 
                                        RestInformation entityInfo, 
                                       QueryInformation queryInfo,
                                  Map<String, String[]> parameterValues) {
        this.crudServiceRegistry = crudServiceRegistry;
        this.conversionService = conversionService;
        this.entityInfo = entityInfo;
        this.queryInfo = queryInfo;
        this.parameterValues = parameterValues;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public T findOne() {
        InvokeableMethod invokable = findInvokableMethod((Class) queryInfo.getEntityType());
        return (T) invoke(invokable, new HashMap<Class<?>, Object>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> findAll(Sort sort) {
        InvokeableMethod invokable = findInvokableMethod(Iterable.class, Sort.class);
        Map<Class<?>, Object> providedValues = new HashMap<Class<?>, Object>();
        providedValues.put(Sort.class, sort);
        return (List<T>) invoke(invokable, providedValues);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Page<T> findAll(Pageable pageable) {
        InvokeableMethod invokable = findInvokableMethod(Page.class, Pageable.class, Sort.class);
        Map<Class<?>, Object> providedValues = new HashMap<Class<?>, Object>();
        providedValues.put(Pageable.class, pageable);
        providedValues.put(Sort.class, pageable.getSort());
        return (Page<T>) invoke(invokable, providedValues);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getEntityClass() {
        return entityInfo.getEntityClass();
    }
    
    // Method retrieval

    private InvokeableMethod findInvokableMethod(Class<?> returnType, Class<?>... preferredTypes) {
        final Class entityClass = entityInfo.getEntityClass();
        
        Object service = crudServiceRegistry.getService(entityClass);
        InvokeableMethod method = findInvokableMethod(service, returnType, preferredTypes);
        if (method == null) {
            Object repository = crudServiceRegistry.getRepository((Class) queryInfo.getEntityType());
            method = findInvokableMethod(repository, returnType, preferredTypes);
        }
        return Preconditions.checkNotNull(method, "Could not find custom finder method '" + queryInfo.getMethodName() + "' for " + entityClass.getName());
    }
    
    private InvokeableMethod findInvokableMethod(Object bean, Class<?> returnType, Class<?>[] preferredTypes) {
        List<Class<?>> targetClasses = getAllTargetClasses(bean);
        for (Class<?> targetClass : targetClasses) {
            Method method = findMethod(targetClass, returnType, preferredTypes);
            if (method != null) {
                return new InvokeableMethod(bean, method);
            }
        }
        return null;
    }

    private List<Class<?>> getAllTargetClasses(Object bean) {
        List<Class<?>> candidateClasses = new ArrayList<Class<?>>();
        candidateClasses.add(AopUtils.getTargetClass(bean));
        for (Class<?> interfaceClass : bean.getClass().getInterfaces()) {
            candidateClasses.add(interfaceClass);
        }
        return candidateClasses;
    }
    
    private Method findMethod(Class<?> candidateClass, Class<?> returnType, Class<?>[] preferredTypes) {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(candidateClass);
        List<Method> found = new ArrayList<Method>();
        for (Method method : methods) {
            if (!isProxyClass(method.getDeclaringClass()) &&
                method.getName().equals(queryInfo.getMethodName()) && 
                returnType.isAssignableFrom(method.getReturnType())) {
                found.add(method);
            }
        }
        return findMethodWithPreferredParameterTypes(found, preferredTypes);
    }

    private boolean isProxyClass(Class<?> declaringClass) {
        return declaringClass.getSimpleName().contains("$$");
    }

    /**
     * Return the first method with a preferred parameter type. Whenever no
     * method is preferred we just return the first method.
     * 
     * @param methods the suitable methods
     * @param preferredTypes the preferred types, in order of preference
     * @return the first preferred method
     */
    private Method findMethodWithPreferredParameterTypes(List<Method> methods, Class<?>[] preferredTypes) {
        if (methods.isEmpty()) {
            return null;
        }
        
        for (Class<?> preferredType : preferredTypes) {
            for (Method method : methods) {
                if (ArrayUtils.contains(method.getParameterTypes(), preferredType)) {
                    return method;
                }
            }
        }
        return methods.get(0);
    }
    
    // Method invocation

    private Object invoke(InvokeableMethod invokable, Map<Class<?>, Object> providedValues) {
        Object[] args = buildArguments(invokable, providedValues);
        try {
            return invokable.invoke(args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not invoke finder method.", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Could not invoke finder method.", e);
        }
    }

    private <P> Object[] buildArguments(InvokeableMethod invokable, Map<Class<?>, Object> providedValues) {
        final Class<?>[] parameterTypes = invokable.method.getParameterTypes();
        final List<String> parameterNames = queryInfo.getParameterNames();

        Object[] args = new Object[parameterTypes.length];
        for (int index = 0; index < parameterTypes.length; index++) {
            String currentName = (parameterNames.size() - 1) >= index ? parameterNames.get(index) : "";
            Class<?> currentType = parameterTypes[index];

            Object currentValue = null;
            if (StringUtils.isNotBlank(currentName)) {
                currentValue = conversionService.convert(parameterValues.get(currentName), currentType);
            } else if (providedValues.containsKey(currentType)) {
                currentValue = providedValues.get(currentType);
            }
            args[index] = currentValue;
        }
        return args;
    }

    private static class InvokeableMethod {
        
        private final Object target;
        
        private final Method method;
        
        public InvokeableMethod(Object target, Method method) {
            this.target = target;
            this.method = method;
        }
        
        public Object invoke(Object... args) throws IllegalAccessException, InvocationTargetException {
            // TODO: Handle exception when target is not the declarer of the method
            return ReflectionUtils.invokeMethod(method, target, args);
        }
        
    }

}

/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service.adapter;

import io.restzilla.service.Listable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.support.Repositories;

/**
 * Listable that performs a custom method by reflection. 
 *
 * @author Jeroen van Schagen
 * @since Dec 9, 2015
 */
public class RepositoryMethodListable<T> implements Listable<T> {
    
    private final Class<?> resultType;
    
    private final Repositories repositories;
    
    private final String methodName;
    
    private final Map<String, String[]> parameterValues;

    public RepositoryMethodListable(ApplicationContext applicationContext, Class<?> resultType, String methodName, Map<String, String[]> parameterValues) {
        this.repositories = new Repositories(applicationContext);
        this.resultType = resultType;
        this.methodName = methodName;
        this.parameterValues = parameterValues;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> findAll(Sort sort) {
        // TODO Auto-generated method stub
        return new ArrayList<T>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<T> findAll(Pageable pageable) {
        // TODO Auto-generated method stub
        return new PageImpl<T>(new ArrayList<T>(), pageable, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getEntityClass() {
        return resultType;
    }
    
}

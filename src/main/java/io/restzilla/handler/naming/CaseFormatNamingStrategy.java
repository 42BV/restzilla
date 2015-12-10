/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.handler.naming;

import com.google.common.base.CaseFormat;

/**
 * Default implementation of the naming strategy.
 *
 * @author Jeroen van Schagen
 * @since Nov 10, 2015
 */
public class CaseFormatNamingStrategy implements RestNamingStrategy {
    
    private final CaseFormat format;
    
    public CaseFormatNamingStrategy(CaseFormat format) {
        this.format = format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBasePath(Class<?> entityClass) {
        String simpleName = entityClass.getSimpleName();
        return CaseFormat.UPPER_CAMEL.converterTo(format).convert(simpleName);
    }
    
}

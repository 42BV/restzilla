/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.handler.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

/**
 * Spring security provider implementation.
 *
 * @author Jeroen van Schagen
 * @since Sep 8, 2015
 */
public class SpringExpressionSecurityProvider implements SecurityProvider {
    
    @Autowired(required = false)
    private DefaultWebSecurityExpressionHandler handler;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthorized(String[] expressions, HttpServletRequest request) {
        boolean authorized = true;
        if (expressions.length > 0) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            FilterInvocation invocation = new FilterInvocation(request.getServletPath(), request.getMethod());
            EvaluationContext context = getHandler().createEvaluationContext(authentication, invocation);
            for (String expression : expressions) {
                ExpressionParser parser = getHandler().getExpressionParser();
                if (!ExpressionUtils.evaluateAsBoolean(parser.parseExpression(expression), context)) {
                    return false;
                }
            }
        }
        return authorized;
    }
    
    private DefaultWebSecurityExpressionHandler getHandler() {
        if (handler == null) {
            handler = new DefaultWebSecurityExpressionHandler();
        }
        return handler;
    }

}

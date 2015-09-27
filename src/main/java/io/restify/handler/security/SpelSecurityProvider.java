/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.handler.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

/**
 * Implementation that evaluates SPEL expressions.
 *
 * @author Jeroen van Schagen
 * @since Sep 8, 2015
 */
public class SpelSecurityProvider implements SecurityProvider {
    
    /**
     * Web security expression handler.
     */
    private DefaultWebSecurityExpressionHandler handler = new DefaultWebSecurityExpressionHandler();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthorized(String[] expressions, HttpServletRequest request) {
        boolean authorized = true;
        if (expressions.length > 0) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                authentication = new AnonymousAuthenticationToken("?", "?", AuthorityUtils.createAuthorityList("?"));
            }

            FilterInvocation invocation = new FilterInvocation(request.getServletPath(), request.getMethod());
            EvaluationContext context = handler.createEvaluationContext(authentication, invocation);
            for (String expression : expressions) {
                ExpressionParser parser = handler.getExpressionParser();
                if (!ExpressionUtils.evaluateAsBoolean(parser.parseExpression(expression), context)) {
                    return false;
                }
            }
        }
        return authorized;
    }
    
    /**
     * Configure the default web security expression handler.
     * @param handler the handler to set
     */
    @Autowired(required = false)
    public void setHandler(DefaultWebSecurityExpressionHandler handler) {
        this.handler = handler;
    }

}

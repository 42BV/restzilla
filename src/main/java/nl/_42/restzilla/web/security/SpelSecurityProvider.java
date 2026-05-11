/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.web.security;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * Implementation that evaluates SPEL expressions.
 *
 * @author Jeroen van Schagen
 * @since Sep 8, 2015
 */
public class SpelSecurityProvider implements SecurityProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthorized(String[] expressions, HttpServletRequest request) {
        boolean authorized = true;
        if (expressions.length > 0) {
            RequestAuthorizationContext context = new RequestAuthorizationContext(request);
            Authentication authentication = getAuthentication(request);
            for (String expression : expressions) {
                if (StringUtils.isNotBlank(expression)) {
                    WebExpressionAuthorizationManager manager = new WebExpressionAuthorizationManager(expression);
                    AuthorizationResult result = manager.authorize(() -> authentication, context);
                    if (!result.isGranted()) {
                        return false;
                    }
                }
            }
        }
        return authorized;
    }

    private Authentication getAuthentication(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal instanceof Authentication authentication) {
            return authentication;
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                authentication = annonymous();
            }
            return authentication;
        }
    }

    private AnonymousAuthenticationToken annonymous() {
        return new AnonymousAuthenticationToken("anonymousUser", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    }

}

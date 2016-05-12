package io.restzilla.util;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Default implementation of the validator, does nothing.
 * Used as default when no validator beans are configured.
 *
 * @author Jeroen van Schagen
 * @since Nov 20, 2015
 */
public class NoOpValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public void validate(Object target, Errors errors) { 
    }
    
}
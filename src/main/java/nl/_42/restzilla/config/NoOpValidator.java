package nl._42.restzilla.config;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

class NoOpValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public void validate(Object target, Errors errors) {
    }

}

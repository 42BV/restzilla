package io.restzilla.config;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;

public class NoOpValidatorTest {
    
    private NoOpValidator validator = new NoOpValidator();
    
    @Test
    public void testSupport() {
        Assert.assertTrue(validator.supports(String.class));
    }
    
    @Test
    public void testValidate() {
        MyClass target = new MyClass();
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(target, "input");
        validator.validate(target, errors);
        Assert.assertFalse(errors.hasErrors());
    }
    
    private static class MyClass {
    }
    
}

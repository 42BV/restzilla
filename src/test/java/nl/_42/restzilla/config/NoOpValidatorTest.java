package nl._42.restzilla.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;

public class NoOpValidatorTest {
    
    private NoOpValidator validator = new NoOpValidator();
    
    @Test
    public void testSupport() {
        Assertions.assertTrue(validator.supports(String.class));
    }
    
    @Test
    public void testValidate() {
        MyClass target = new MyClass();
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(target, "input");
        validator.validate(target, errors);
        Assertions.assertFalse(errors.hasErrors());
    }
    
    private static class MyClass {
    }
    
}

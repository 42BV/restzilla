package nl._42.restzilla.web;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SecurityException.class)
    public ModelAndView handleSecurityException(HttpServletResponse response, SecurityException ex) {
        logger.error("Security exception", ex);
        return error(response, FORBIDDEN);
    }

    @ExceptionHandler(BindException.class)
    public ModelAndView handleBindException(HttpServletResponse response, BindException ex) {
        logger.error("Bind exception", ex);
        return error(response, UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(HttpServletResponse response, Exception ex) {
        logger.error("Global exception", ex);
        return error(response, INTERNAL_SERVER_ERROR);
    }

    private static ModelAndView error(HttpServletResponse response, HttpStatus status) {
        response.setStatus(status.value());
        return new ModelAndView(new MappingJackson2JsonView(), "error", status.name());
    }

}

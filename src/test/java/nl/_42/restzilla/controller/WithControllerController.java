/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.controller;

import nl._42.restzilla.RestResource;
import nl._42.restzilla.model.WithController;

import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RestResource(entityType = WithController.class)
public class WithControllerController {
    
    @RequestMapping(value = "/with-controller", method = RequestMethod.GET)
    public Map<String, String> test() {
        return Collections.singletonMap("a", "b");
    }

}

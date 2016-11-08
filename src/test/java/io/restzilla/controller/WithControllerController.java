/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.controller;

import io.restzilla.RestResource;
import io.restzilla.model.WithController;

import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/with-controller")
@RestResource(entityType = WithController.class)
public class WithControllerController {
    
    @RequestMapping(method = RequestMethod.GET)
    public Map<String, String> test() {
        return Collections.singletonMap("a", "b");
    }

}

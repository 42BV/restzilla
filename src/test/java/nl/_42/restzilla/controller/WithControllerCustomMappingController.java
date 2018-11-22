/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.controller;

import nl._42.restzilla.RestResource;
import nl._42.restzilla.model.WithControllerCustomMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/with-controller-custom-test")
@RestResource(entityType = WithControllerCustomMapping.class)
public class WithControllerCustomMappingController {
    
}

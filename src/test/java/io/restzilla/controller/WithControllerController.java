/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.controller;

import io.restzilla.RestResource;
import io.restzilla.model.WithController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/with-controller")
@RestResource(entityClass = WithController.class)
public class WithControllerController {
    
}

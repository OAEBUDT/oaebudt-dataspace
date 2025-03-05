package org.oaebudt.edc.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BaseController {

    @GetMapping("/secure")
    public String testSecureApi(){
        return "Secure Endpoint";
    }

    @GetMapping("/public")
    public String testPublicApi(){
        return "Unsecure Endpoint";
    }
}

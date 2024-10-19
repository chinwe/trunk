package com.learning.spring.samples.controller;

import com.learning.spring.samples.annotation.PermissionCheck;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chinwe
 * 2024/10/19
 */
@RestController
public class HelloController {

    // @PermissionCheck
    @GetMapping("/hello")
    public String hello() {
        return getMessage();
    }

    private String getMessage() {
        return "Hello World. Bravo Spring.";
    }
}

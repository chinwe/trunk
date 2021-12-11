package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chinwe
 * 2021/12/11
 */
@RestController
public class HelloController {

    @RequestMapping("/jvm")
    public String helloJvm() {
        return "Hello From Jvm";
    }
}

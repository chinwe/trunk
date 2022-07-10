package com.learn.security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chinwe
 * 2022/4/10
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/v1/login")
    public void login() {
    }

    @GetMapping("/v1/logout")
    public void logout() {
    }
}

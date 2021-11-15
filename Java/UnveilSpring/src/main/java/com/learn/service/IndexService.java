package com.learn.service;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author chinwe
 * 2021/11/14
 */
@Service
public class IndexService {

    @Resource
    UserService userService;

    public IndexService() {
        System.out.println("IndexService Construct");
    }

    @PostConstruct
    public void aa() {
        System.out.println("init");
    }
}

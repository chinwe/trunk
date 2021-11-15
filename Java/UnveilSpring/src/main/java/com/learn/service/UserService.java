package com.learn.service;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author chinwe
 * 2021/11/14
 */
@Service
public class UserService {

    @Resource
    IndexService indexService;

    public UserService() {
        System.out.println("UserService Construct");
    }

    public void getService() {
        System.out.println(indexService);
    }

}

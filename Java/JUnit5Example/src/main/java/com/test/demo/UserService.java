package com.test.demo;

import org.springframework.stereotype.Service;

/**
 * @author chinwe
 * 2024/10/5
 */
@Service
public class UserService {

    public String getUserById(int id) {
        return "User" + id;
    }
}

package com.learn.aop.impl;

import com.learn.aop.IUserDao;
import com.learn.entity.User;
import org.springframework.stereotype.Component;

/**
 * UserDaoImpl
 * @author chinwe
 * 2021/12/26
 */
@Component
public class UserDaoImpl implements IUserDao {

    @Override
    public int add(User user) {
        System.out.println("add user");
        return 0;
    }
}

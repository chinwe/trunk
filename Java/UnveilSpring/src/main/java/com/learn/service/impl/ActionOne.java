package com.learn.service.impl;

import com.learn.service.IAction;
import org.springframework.stereotype.Component;

/**
 * @author chinwe
 * 2021/12/27
 */
@Component
public class ActionOne implements IAction {
    @Override
    public void invoke() {
        System.out.println("ActionOne.invoke");
    }
}

package com.learn.service;

import com.learn.service.condition.WindowsCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

/**
 * @author chinwe
 * 2022/1/21
 */
@Conditional(WindowsCondition.class)
@Service
public class WindowsOnlyService {

    public boolean isWindows() {
        return true;
    }
}

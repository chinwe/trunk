package com.learn.service;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author chinwe
 * 2021/12/27
 */
@Service
public class ActionRouteService {

    @Resource(name = "actServiceMap")
    private Map<String, IAction> actionRoute;

    public void doAction(String action){
        actionRoute.get(action).invoke();
    }

}

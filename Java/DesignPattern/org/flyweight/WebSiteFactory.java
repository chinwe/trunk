package org.flyweight;

import java.util.HashMap;

/**
 * @author mozixun
 * @description
 * @date 2020/3/29 - 11:51 上午
 */
public class WebSiteFactory {

    private HashMap<String, ConcreteWebSite> pool = new HashMap<>();

    public BaseWebSite getWebSiteCategory(String type) {

        if (!pool.containsKey(type)) {
            pool.put(type, new ConcreteWebSite(type));
        }

        return (BaseWebSite) pool.get(type);
    }
}

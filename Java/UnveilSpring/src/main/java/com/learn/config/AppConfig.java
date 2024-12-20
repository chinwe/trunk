package com.learn.config;

import com.learn.service.impl.ActionOne;
import com.learn.service.impl.ActionTwo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chinwe
 * 2021/11/14
 */
@Configuration
@ComponentScan(basePackages = "com.learn")
@EnableAspectJAutoProxy
@EnableAutoConfiguration
@PropertySource("classpath:/config.properties")
public class AppConfig {

    @Bean(name="actServiceMap")
    public Map geneActServiceMap(@Qualifier("actionOne") ActionOne actionOne,
                                 @Qualifier("actionTwo") ActionTwo actionTwo){
        Map map = new HashMap<>();
        map.put(actionOne.getClass().getName(), actionOne);
        map.put(actionTwo.getClass().getName(), actionTwo);
        return map;
    }
}

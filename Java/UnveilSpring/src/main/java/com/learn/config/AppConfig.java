package com.learn.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author chinwe
 * 2021/11/14
 */
@Configuration
@ComponentScan(basePackages = "com.learn")
@EnableAspectJAutoProxy
public class AppConfig {
}

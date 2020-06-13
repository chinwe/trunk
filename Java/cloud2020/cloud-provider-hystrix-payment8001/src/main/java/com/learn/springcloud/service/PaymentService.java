package com.learn.springcloud.service;

import cn.hutool.core.util.IdUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.TimeUnit;

@Service
public class PaymentService {

    public String paymentInfoOk(Integer id) {
        return "Thread" + Thread.currentThread().getName() + " paymentInfoOk, id: " + id + ".";
    }

    @HystrixCommand(fallbackMethod = "paymentInfoTimeoutFallback", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000")
    })
    public String paymentInfoTimeout(Integer id) {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Thread" + Thread.currentThread().getName() + " paymentInfoTimeout, id: " + id + ".";
    }

    public String paymentInfoTimeoutFallback(Integer id) {
        return "Thread" + Thread.currentThread().getName() + " 8001 paymentInfoTimeout, id: " + id + ". fallback :(";
    }

    // 服务熔断
    @HystrixCommand(fallbackMethod = "paymentCircuitBreakerFallback", commandProperties = {
            @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60")
    })
    public String paymentCircuitBreaker(@PathVariable("id") Integer id) {
        if (id < 0) {
            throw new RuntimeException("异常: id 不能为负数");
        }

        String uuid = IdUtil.simpleUUID();
        return Thread.currentThread().getName() + " Successful. uuid: " + uuid;
    }

    public String paymentCircuitBreakerFallback(Integer id) {
        return "id 不能为负数。请修改。id: " + id;
    }
}
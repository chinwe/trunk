package com.learn.springcloud.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.stereotype.Service;

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
        return "Thread" + Thread.currentThread().getName() + " paymentInfoTimeout, id: " + id + ". fallback :(";
    }
}
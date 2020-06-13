package com.learn.springcloud.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class PaymentService {

    public String paymentInfoOk(Integer id) {
        return "Thread" + Thread.currentThread().getName() + " paymentInfoOk, id: " + id + ".";
    }

    public String paymentInfoTimeout(Integer id) {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Thread" + Thread.currentThread().getName() + " paymentInfoTimeout, id: " + id + ".";
    }
}

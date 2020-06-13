package com.learn.springcloud.service.impl;

import com.learn.springcloud.service.IPaymentHystrixService;
import org.springframework.stereotype.Component;

@Component
public class PaymentFallbackService implements IPaymentHystrixService {
    @Override
    public String paymentInfoOk(Integer id) {
        return "paymentInfoOk fallback.";
    }

    @Override
    public String paymentInfoTimeout(Integer id) {
        return "paymentInfoTimeout fallback.";
    }
}

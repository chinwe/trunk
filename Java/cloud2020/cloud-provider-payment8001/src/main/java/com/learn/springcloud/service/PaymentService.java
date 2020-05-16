package com.learn.springcloud.service;

import com.learn.springcloud.entities.Payment;
import org.apache.ibatis.annotations.Param;
import org.springframework.context.annotation.Bean;

public interface PaymentService {

    public int create(Payment payment);

    public Payment getPaymentById(@Param("id") long id);
}

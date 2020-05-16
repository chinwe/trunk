package com.learn.springcloud.service;

import org.apache.ibatis.annotations.Param;
import com.learn.springcloud.entities.Payment;

public interface PaymentService {

    public int create(Payment payment);

    public Payment getPaymentById(@Param("id") long id);
}

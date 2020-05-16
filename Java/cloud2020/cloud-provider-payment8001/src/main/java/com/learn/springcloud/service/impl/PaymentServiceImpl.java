package com.learn.springcloud.service.impl;

import com.learn.springcloud.dao.PaymentDao;
import com.learn.springcloud.service.PaymentService;
import org.apache.ibatis.annotations.Param;
import com.learn.springcloud.entities.Payment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private PaymentDao paymentDao;

    @Override
    public int create(Payment payment) {
        return paymentDao.create(payment);
    }

    @Override
    public Payment getPaymentById(@Param("id") long id) {
        return paymentDao.getPaymentById(id);
    }
}

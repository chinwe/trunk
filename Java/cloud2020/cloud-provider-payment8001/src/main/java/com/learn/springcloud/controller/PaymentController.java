package com.learn.springcloud.controller;

import cn.hutool.log.Log;
import com.learn.springcloud.entities.CommonResult;
import com.learn.springcloud.entities.Payment;
import com.learn.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Slf4j
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @Value("${server.port}")
    private String serverPort;

    @Resource
    private DiscoveryClient discoveryClient;

    @PostMapping(value = "payment/create")
    public CommonResult create(@RequestBody Payment payment) {
        int result = paymentService.create(payment);
        //Log.info("paymentService.create return " + result);
        if (result > 0) {
            return new CommonResult(200, "ok. serverPort: " + serverPort, payment);
        } else {
            return new CommonResult(444, "Failed to insert.");
        }
    }

    @GetMapping(value = "payment/get/{id}")
    public CommonResult getPaymentById(@PathVariable("id") long id) {
        Payment payment = paymentService.getPaymentById(id);
        if (payment != null) {
            return new CommonResult(200, "ok. serverPort: " + serverPort, payment);
        } else {
            return new CommonResult(404, "No result.");
        }
    }

    @GetMapping(value = "payment/discovery")
    public Object discovery() {
        List<String> services = discoveryClient.getServices();
        for (String service : services) {
            Log log = Log.get();
            log.info("service " + service);
        }
        List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
        for (ServiceInstance instance : instances) {
            Log log = Log.get();
            log.info(instance.getServiceId() +
                    "\t" + instance.getHost() +
                    "\t" + instance.getPort() +
                    "\t" + instance.getUri());
        }
        return this.discoveryClient;
    }

    @GetMapping(value = "payment/lb")
    public String paymentLb() {
        return serverPort;
    }
}

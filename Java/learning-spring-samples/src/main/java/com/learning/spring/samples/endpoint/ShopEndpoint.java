package com.learning.spring.samples.endpoint;

import com.learning.spring.samples.config.BinaryTeaProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

/**
 * @author chinwe
 * 2024/11/3
 */
@Component
@Endpoint(id = "shop")
public class ShopEndpoint {
    private final BinaryTeaProperties binaryTeaProperties;

    public ShopEndpoint(ObjectProvider<BinaryTeaProperties> binaryTeaProperties) {
        this.binaryTeaProperties = binaryTeaProperties.getIfAvailable();
    }

    @ReadOperation
    public String state() {
        if (binaryTeaProperties == null || !binaryTeaProperties.isReady()) {
            return "We're not ready.";
        } else {
            return "We open " + binaryTeaProperties.getOpenHours() + ".";
        }
    }
}
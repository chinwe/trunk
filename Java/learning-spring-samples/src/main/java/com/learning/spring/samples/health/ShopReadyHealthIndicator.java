package com.learning.spring.samples.health;

import com.learning.spring.samples.config.BinaryTeaProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

/**
 * @author chinwe
 * 2024/11/3
 */
@Component
public class ShopReadyHealthIndicator extends AbstractHealthIndicator {
    private final BinaryTeaProperties binaryTeaProperties;

    public ShopReadyHealthIndicator(ObjectProvider<BinaryTeaProperties> binaryTeaProperties) {
        this.binaryTeaProperties = binaryTeaProperties.getIfAvailable();
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        if (binaryTeaProperties == null || !binaryTeaProperties.isReady()) {
            builder.down();
        } else {
            builder.up();
        }
    }
}

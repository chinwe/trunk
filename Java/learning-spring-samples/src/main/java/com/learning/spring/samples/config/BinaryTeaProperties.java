package com.learning.spring.samples.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author chinwe
 * 2024/10/27
 */
@ConfigurationProperties(prefix = "binarytea")
@Data
public class BinaryTeaProperties {
    private boolean ready;
    private String openHours;
}

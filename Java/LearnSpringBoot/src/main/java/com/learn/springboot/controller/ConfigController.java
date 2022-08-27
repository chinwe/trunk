package com.learn.springboot.controller;

import com.learn.springboot.controller.vo.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chinwe
 * 2022/8/27
 */
@RestController
@RequestMapping("/config")
public class ConfigController {

    @GetMapping("/v1/values")
    public CommonResponse getConfigInfo() {
        Map<String, Object> configValues = new HashMap<>(16);
        configValues.put("common.key1", true);
        configValues.put("common.key2", 2);
        configValues.put("common.key3", "test");

        return CommonResponse.builder()
                .code("200")
                .msg("ok")
                .data(configValues)
                .build();
    }
}

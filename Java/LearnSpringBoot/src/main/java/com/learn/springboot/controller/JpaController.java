package com.learn.springboot.controller;

import com.learn.springboot.controller.vo.CommonResponse;
import com.learn.springboot.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author chinwe
 * 2023/9/9
 */
@RestController
@RequestMapping("/data")
public class JpaController {

    @Resource
    private UserRepository userRepository;

    @GetMapping("/v1/exist")
    public CommonResponse getConfigInfo() {
        boolean result = userRepository.existsByNameAndPassword("zhangsan", "abc12345");

        return CommonResponse.builder()
                .code("200")
                .msg("ok")
                .data(Optional.of(result))
                .build();
    }
}

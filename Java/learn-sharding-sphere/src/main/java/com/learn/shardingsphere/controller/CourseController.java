package com.learn.shardingsphere.controller;

import com.learn.shardingsphere.service.CourseService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author chinwe
 * 2022/3/19
 */
@RestController
public class CourseController {

    @Resource
    private CourseService courseService;

    @GetMapping("/hello")
    String hello() {
        return "hello fdsfsf";
    }

    @PostMapping("/v1/courses/add")
    void addCourse() {
        courseService.addCourse();
    }

}

package com.learn.shardingsphere.service.impl;

import com.learn.shardingsphere.repository.CourseRepository;
import com.learn.shardingsphere.repository.entity.Course;
import com.learn.shardingsphere.service.CourseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author chinwe
 * 2022/3/19
 */
@Service
public class CourseServiceImpl implements CourseService {

    @Resource
    private CourseRepository courseRepository;

    @Override
    public void addCourse() {
        final Course course = new Course();
        course.setCname("cs");
        course.setUserId(1L);
        course.setStatus("ok");

        courseRepository.save(course);
    }
}

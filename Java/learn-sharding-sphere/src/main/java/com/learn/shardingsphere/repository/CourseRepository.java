package com.learn.shardingsphere.repository;

import com.learn.shardingsphere.repository.entity.Course;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author chinwe
 * 2022/3/19
 */
@Repository
public interface CourseRepository extends CrudRepository<Course, Long> {

}

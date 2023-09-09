package com.learn.springboot.repository;

import com.learn.springboot.repository.enitity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByNameAndPassword(String username, String password);

}
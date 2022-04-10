package com.learn.security.repository;

import com.learn.security.repository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author chinwe
 * 2022/4/10
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByName(String name);
}
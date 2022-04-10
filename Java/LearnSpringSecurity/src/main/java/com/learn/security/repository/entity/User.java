package com.learn.security.repository.entity;

import lombok.*;

import javax.persistence.*;

/**
 * @author chinwe
 * 2022/4/10
 */
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(generator = "uuid")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "password", nullable = false)
    private String password;
}

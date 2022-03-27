package com.learn.shardingsphere.repository.entity;

import lombok.*;

import javax.persistence.*;

/**
 * @author chinwe
 * 2022/3/19
 */
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cid", nullable = false)
    private Long cid;

    @Column(name = "cname", nullable = false)
    private String cname;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "status", nullable = false)
    private String status;
}

package com.learn.springboot.repository.enitity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User implements Serializable {

    @Id
    @GeneratedValue
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

}
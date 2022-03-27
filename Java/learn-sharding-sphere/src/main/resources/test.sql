
create database course_db;

use course_db;

create table course_1 (
    cid bigint(20) primary key ,
    cname varchar(50) not null,
    user_id bigint(20) not null ,
    status varchar(10) not null
) engine = InnoDB;

create table course_2 (
    cid bigint(20) primary key ,
    cname varchar(50) not null,
    user_id bigint(20) not null ,
    status varchar(10) not null
) engine = InnoDB;

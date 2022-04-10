
create database dev;

use dev;

create table user (
    id VARCHAR(64) primary key,
    name varchar(64) not null,
    password varchar(64) not null
) engine = InnoDB;

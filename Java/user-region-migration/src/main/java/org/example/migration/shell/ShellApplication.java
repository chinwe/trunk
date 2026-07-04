package org.example.migration.shell;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 迁移框架启动入口。
 * Spring Shell 的命令通过 @ShellComponent + @Command 注册，由自动配置装配。
 */
@SpringBootApplication
public class ShellApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShellApplication.class, args);
    }
}

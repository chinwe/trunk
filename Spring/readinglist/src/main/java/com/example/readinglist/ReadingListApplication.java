package com.example.readinglist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class ReadingListApplication {

    @Autowired
    private InitTest initTest;

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(ReadingListApplication.class, args);

        ReadingListApplication app = applicationContext.getBean(com.example.readinglist.ReadingListApplication.class);
        app.proxyInit();
    }

    public void proxyInit() {
        initTest.init();
    }
}

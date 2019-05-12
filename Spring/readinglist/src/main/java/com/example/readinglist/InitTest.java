package com.example.readinglist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Component
public class InitTest {

    @Autowired
    private ReadingListRepository readingListRepository;

    public void init() {
        List<Book> bookList = readingListRepository.findByReader("test");

        System.out.println("init");
    }
}

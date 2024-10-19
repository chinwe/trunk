package com.test.demo;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

@DisplayName("内嵌测试类")
public class NestUnitTest {
    @BeforeEach
    void init() {
     System.out.println("测试方法执行前准备");
    }

    @Nested
    @DisplayName("第一个内嵌测试类")
    class FirstNestTest {
         @Test
         void test() {
          System.out.println("第一个内嵌测试类执行测试");
         }
    }

    @Nested
    @DisplayName("第二个内嵌测试类")
    class SecondNestTest {
        @Test
        void test() {
         System.out.println("第二个内嵌测试类执行测试");
        }
    }

    @Test
    void mockAndSpy() {
        List<String> mockList = Mockito.spy(new ArrayList<>());
        mockList.add("A");
        mockList.add("B");
        Assertions.assertEquals("A", mockList.getFirst());

        mockList = Mockito.mock(List.class);
        Mockito.when(mockList.size()).thenReturn(100);
        Assertions.assertEquals(100, mockList.size());
    }
}
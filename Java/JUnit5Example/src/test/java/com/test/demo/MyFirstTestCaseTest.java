package com.test.demo;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@DisplayName("我的第一个测试用例")
public class MyFirstTestCaseTest {

    @BeforeAll
    public static void init() {
        System.out.println("初始化数据");
    }

    @AfterAll
    public static void cleanup() {
        System.out.println("清理数据");
    }

    @BeforeEach
    public void tearup() {
        System.out.println("当前测试方法开始");
    }

    @AfterEach
    public void tearDown() {
        System.out.println("当前测试方法结束");
    }

    @DisplayName("我的第一个测试")
    @Test
    void testFirstTest() {
        System.out.println("我的第一个测试开始测试");
    }

    @DisplayName("我的第二个测试")
    @Test
    void testSecondTest() {
        System.out.println("我的第二个测试开始测试");
    }

    @DisplayName("自定义名称重复测试")
    @RepeatedTest(value = 3, name = "{displayName} 第 {currentRepetition} 次")
    public void i_am_a_repeated_test_2() {
        System.out.println("执行测试");
    }

    @Test
    void testGroupAssertions() {
     int[] numbers = {0, 1, 2, 3, 4};
     Assertions.assertAll("numbers",
             () -> Assertions.assertEquals(numbers[1], 1),
             () -> Assertions.assertEquals(numbers[3], 3),
             () -> Assertions.assertEquals(numbers[4], 4)
     );
    }

    @Test
    @DisplayName("超时方法测试")
    void test_should_complete_in_one_second() {
     Assertions.assertTimeoutPreemptively(Duration.of(3, ChronoUnit.SECONDS), () -> Thread.sleep(2000));
    }

    @Test
    @DisplayName("测试捕获的异常")
    void assertThrowsException() {
     String str = null;
     Assertions.assertThrows(IllegalArgumentException.class, () -> {
      Integer.valueOf(str);
     });
    }
}
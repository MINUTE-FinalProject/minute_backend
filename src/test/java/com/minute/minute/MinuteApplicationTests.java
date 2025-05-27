package com.minute.minute;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.minute") // 이걸로 com.minute 하위 전부 스캔
public class MinuteApplicationTests {
    public static void main(String[] args) {
        SpringApplication.run(MinuteApplicationTests.class, args);
    }
}

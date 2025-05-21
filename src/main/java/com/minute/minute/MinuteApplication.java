package com.minute.minute;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.minute.minute")
public class MinuteApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinuteApplication.class, args);
    }

}

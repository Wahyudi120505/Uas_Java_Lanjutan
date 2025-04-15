package com.example.hay_mart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HayMartApplication {
    public static void main(String[] args) {
        SpringApplication.run(HayMartApplication.class, args);
    }
}

package com.xb.pay.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.xb.pay")
@EnableScheduling
@EnableAsync
public class PayDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayDemoApplication.class, args);
    }
}

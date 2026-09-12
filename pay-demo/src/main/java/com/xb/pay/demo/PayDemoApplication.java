package com.xb.pay.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 支付教学项目 —— 启动类
 * <p>作者：xb | 日期：2026-09-12</p>
 */
@SpringBootApplication(scanBasePackages = "com.xb.pay")
public class PayDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayDemoApplication.class, args);
    }
}
package com.xb.pay.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * PayDemoApplication - 支付教学演示项目启动类
 *
 * 扫描 com.xb.pay 全量包，启用定时任务（@EnableScheduling）
 * 和异步事件处理（@EnableAsync），驱动支付核心流程。
 *
 * @author ibqy
 */
@SpringBootApplication(scanBasePackages = "com.xb.pay")
@EnableScheduling
@EnableAsync
public class PayDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayDemoApplication.class, args);
    }
}

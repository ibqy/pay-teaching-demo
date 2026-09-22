package com.xb.pay.demo.config;

import com.xb.pay.alipay.config.AlipayConfig;
import com.xb.pay.wechat.config.WechatPayConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PayConfig - 支付配置类，从 application.yml 读取并注入各渠道配置
 *
 * 通过 @ConfigurationProperties 将 YAML 配置映射到 AlipayConfig 和 WechatPayConfig。
 * 生产环境私钥等敏感信息应从 KMS 或环境变量读取。
 *
 * @author ibqy
 */
@Configuration
public class PayConfig {

    /**
     * 创建支付宝配置 Bean，从 pay.alipay 前缀读取配置
     * @return 支付宝配置对象
     */
    @Bean
    @ConfigurationProperties(prefix = "pay.alipay")
    public AlipayConfig alipayConfig() {
        return new AlipayConfig();
    }

    /**
     * 创建微信支付配置 Bean，从 pay.wechat 前缀读取配置
     * @return 微信支付配置对象
     */
    @Bean
    @ConfigurationProperties(prefix = "pay.wechat")
    public WechatPayConfig wechatPayConfig() {
        return new WechatPayConfig();
    }
}
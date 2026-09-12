package com.xb.pay.demo.config;

import com.xb.pay.alipay.config.AlipayConfig;
import com.xb.pay.wechat.config.WechatPayConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付配置
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>知识点</b>：生产环境中配置应从配置文件加密读取，
 * 私钥等敏感信息可存储在密钥管理服务（KMS）或环境变量中。</p>
 */
@Configuration
public class PayConfig {

    @Bean
    @ConfigurationProperties(prefix = "pay.alipay")
    public AlipayConfig alipayConfig() {
        return new AlipayConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = "pay.wechat")
    public WechatPayConfig wechatPayConfig() {
        return new WechatPayConfig();
    }
}
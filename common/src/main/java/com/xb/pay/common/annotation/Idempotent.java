package com.xb.pay.common.annotation;

import java.lang.annotation.*;

/**
 * Idempotent - 幂等性注解，防止支付接口重复提交
 *
 * 在支付场景中，网络抖动可能导致同一笔请求被发送多次。
 * 通过标记此注解，配合 AOP 切面实现基于 TTL 的幂等保护。
 *
 * @author ibqy
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    /** SpEL 表达式，用于生成幂等 key */
    String key();
    /** 幂等窗口期，单位秒，默认 600 秒（10 分钟） */
    int ttl() default 600;
}

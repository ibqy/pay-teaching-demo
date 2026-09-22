package com.xb.pay.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * OrderNoGenerator - 订单号生成器（教学简易版）
 *
 * 生产环境需用足唯一性、防推断、可读性三要素，
 * 常用雪花算法（Snowflake）或数据库序列。此处用时间戳+原子序列演示基本思路。
 *
 * @author ibqy
 */
public class OrderNoGenerator {

    private static final AtomicLong seq = new AtomicLong(1);

    /** 生成格式：yyyyMMddHHmmss + 6位序列 */
    public static String next() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return ts + String.format("%06d", seq.getAndIncrement() % 1000000);
    }
}
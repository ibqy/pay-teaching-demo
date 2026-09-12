package com.xb.pay.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 订单号生成器（教学简易版）
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>知识点</b>：生产环境订单号要满足：
 * <ul>
 *   <li>唯一性：全局不重复，一般用雪花算法（Snowflake）或数据库序列</li>
 *   <li>防推断：不包含连续数字，防止恶意刷单</li>
 *   <li>可读性：含时间戳便于排查</li>
 * </ul></p>
 */
public class OrderNoGenerator {

    private static final AtomicLong seq = new AtomicLong(1);

    /** 生成格式：yyyyMMddHHmmss + 6位序列 */
    public static String next() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return ts + String.format("%06d", seq.getAndIncrement() % 1000000);
    }
}
package com.xb.pay.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderNoGeneratorTest - 订单号生成器单元测试
 *
 * 验证生成格式（20位）、唯一性和序列递增等核心特性。
 *
 * @author ibqy
 */
@DisplayName("OrderNoGenerator 订单号生成器测试")
class OrderNoGeneratorTest {

    @Test
    @DisplayName("生成格式：14位时间戳 + 6位序列 = 20位")
    void next_shouldReturn20Chars() {
        String orderNo = OrderNoGenerator.next();
        assertEquals(20, orderNo.length(), "订单号应为20位");
    }

    @Test
    @DisplayName("前14位为纯数字时间戳")
    void next_timestampPartShouldBeDigits() {
        String orderNo = OrderNoGenerator.next();
        String timestamp = orderNo.substring(0, 14);
        assertTrue(timestamp.matches("\\d{14}"), "前14位应为数字");
    }

    @Test
    @DisplayName("后6位为纯数字序列号")
    void next_sequencePartShouldBeDigits() {
        String orderNo = OrderNoGenerator.next();
        String seq = orderNo.substring(14);
        assertTrue(seq.matches("\\d{6}"), "后6位应为数字");
    }

    @Test
    @DisplayName("连续生成100个订单号不重复")
    void next_shouldBeUnique() {
        Set<String> orders = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            orders.add(OrderNoGenerator.next());
        }
        assertEquals(100, orders.size(), "100个订单号应全部唯一");
    }

    @Test
    @DisplayName("序列号部分递增")
    void next_sequenceShouldIncrement() {
        String first = OrderNoGenerator.next();
        String second = OrderNoGenerator.next();
        int seq1 = Integer.parseInt(first.substring(14));
        int seq2 = Integer.parseInt(second.substring(14));
        assertEquals(seq1 + 1, seq2, "序列号应递增");
    }
}

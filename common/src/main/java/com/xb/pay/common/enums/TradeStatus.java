package com.xb.pay.common.enums;

import java.util.Map;
import java.util.Set;

/**
 * 交易状态 —— 支付状态机
 *
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>知识点</b>：支付状态机流转关系。
 * WAITING → SUCCESS / FAILED → REFUNDING → REFUNDED
 * WAITING → CLOSED（用户主动取消或超时关单）</p>
 *
 * <p><b>高阶实战</b>：{@link #canTransitionTo} 提供状态守卫，
 * 防止非法操作（如对 WAITING 订单退款、对 SUCCESS 订单关单）。
 * 生产系统中，状态机是资金安全的最后一道防线。</p>
 */
public enum TradeStatus {
    WAITING,     // 待支付（已下单，二维码已展示）
    SUCCESS,     // 支付成功
    FAILED,      // 支付失败
    REFUNDING,   // 退款中
    REFUNDED,    // 已退款
    CLOSED;      // 已关闭（超时/取消）

    private static final Map<TradeStatus, Set<TradeStatus>> TRANSITIONS = Map.of(
            WAITING,   Set.of(SUCCESS, FAILED, CLOSED),
            SUCCESS,   Set.of(REFUNDING),
            FAILED,    Set.of(),
            REFUNDING, Set.of(REFUNDED),
            REFUNDED,  Set.of(),
            CLOSED,    Set.of()
    );

    /**
     * 判断当前状态是否可以流转到目标状态。
     *
     * <p>示例：</p>
     * <ul>
     *   <li>{@code WAITING.canTransitionTo(SUCCESS)} → true</li>
     *   <li>{@code WAITING.canTransitionTo(REFUNDING)} → false（未支付不能退款）</li>
     *   <li>{@code SUCCESS.canTransitionTo(REFUNDING)} → true</li>
     *   <li>{@code REFUNDED.canTransitionTo(SUCCESS)} → false（终态不可变）</li>
     * </ul>
     */
    public boolean canTransitionTo(TradeStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
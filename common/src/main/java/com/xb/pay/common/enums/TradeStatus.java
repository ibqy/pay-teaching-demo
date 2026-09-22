package com.xb.pay.common.enums;

import java.util.Map;
import java.util.Set;

/**
 * TradeStatus - 交易状态枚举（支付状态机）
 *
 * 状态流转：WAITING → SUCCESS/FAILED → REFUNDING → REFUNDED，
 * WAITING → CLOSED（超时关单）。{@link #canTransitionTo} 提供状态守卫，
 * 是资金安全的最后一道防线。
 *
 * @author ibqy
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
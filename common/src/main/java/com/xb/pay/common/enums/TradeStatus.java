package com.xb.pay.common.enums;

/**
 * 交易状态
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>知识点</b>：支付状态机流转关系。
 * WAITING → SUCCESS / FAILED → REFUNDING → REFUNDED
 * WAITING → CLOSED（用户主动取消或超时关单）</p>
 */
public enum TradeStatus {
    WAITING,     // 待支付（已下单，二维码已展示）
    SUCCESS,     // 支付成功
    FAILED,      // 支付失败
    REFUNDING,   // 退款中
    REFUNDED,    // 已退款
    CLOSED       // 已关闭（超时/取消）
}
package com.xb.pay.common.model;

import com.xb.pay.common.enums.TradeStatus;

import java.time.LocalDateTime;

/**
 * 退款响应
 * <p>作者：xb | 日期：2026-09-12</p>
 */
public class RefundResponse {

    private String outTradeNo;
    private String refundNo;
    private TradeStatus status;
    private LocalDateTime refundedAt;

    public String outTradeNo() { return outTradeNo; }
    public String refundNo() { return refundNo; }
    public TradeStatus status() { return status; }
    public LocalDateTime refundedAt() { return refundedAt; }

    public void setOutTradeNo(String v) { outTradeNo = v; }
    public void setRefundNo(String v) { refundNo = v; }
    public void setStatus(TradeStatus v) { status = v; }
    public void setRefundedAt(LocalDateTime v) { refundedAt = v; }
}
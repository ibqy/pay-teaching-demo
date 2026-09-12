package com.xb.pay.common.model;

import com.xb.pay.common.enums.TradeStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款请求
 * <p>作者：xb | 日期：2026-09-12</p>
 */
public class RefundRequest {

    /** 原商户订单号 */
    private String outTradeNo;
    /** 退款金额（≤ 原支付金额） */
    private BigDecimal refundAmount;
    /** 退款原因（微信必填，支付宝选填） */
    private String reason;

    public String outTradeNo() { return outTradeNo; }
    public BigDecimal refundAmount() { return refundAmount; }
    public String reason() { return reason; }

    public void setOutTradeNo(String v) { outTradeNo = v; }
    public void setRefundAmount(BigDecimal v) { refundAmount = v; }
    public void setReason(String v) { reason = v; }
}
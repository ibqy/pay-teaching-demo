package com.xb.pay.common.model;

import com.xb.pay.common.enums.TradeStatus;

import java.time.LocalDateTime;

/**
 * RefundResponse - 退款响应模型
 *
 * 退款可能是异步的：状态为 REFUNDED 表示即时退款成功，
 * REFUNDING 表示退款受理中，需后续查询确认最终结果。
 *
 * @author ibqy
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
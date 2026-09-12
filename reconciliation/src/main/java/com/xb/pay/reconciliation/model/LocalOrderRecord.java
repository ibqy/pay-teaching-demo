package com.xb.pay.reconciliation.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 本地订单宽口径 — 从本地支付订单表查询出的记录
 * 只提取对账关心的字段，不需要整张订单的全部属性。
 * 关联键：outTradeNo（商户订单号），与 ChannelBillRecord.outTradeNo 做 JOIN
 */
public class LocalOrderRecord {

    private String outTradeNo;              // 商户订单号（关联键）
    private BigDecimal payAmount;           // 支付金额（元）
    private LocalDateTime payTime;          // 支付时间
    private String tradeStatus;             // 订单状态（PAID / REFUNDED 等）
    private String channelTransId;          // 渠道流水号（冗余，可辅助比对）

    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }
    public String getTradeStatus() { return tradeStatus; }
    public void setTradeStatus(String tradeStatus) { this.tradeStatus = tradeStatus; }
    public String getChannelTransId() { return channelTransId; }
    public void setChannelTransId(String channelTransId) { this.channelTransId = channelTransId; }

    @Override
    public String toString() {
        return "LocalOrderRecord{" +
                "outTradeNo='" + outTradeNo + '\'' +
                ", payAmount=" + payAmount +
                ", tradeStatus='" + tradeStatus + '\'' +
                '}';
    }
}
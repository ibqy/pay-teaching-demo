package com.xb.pay.reconciliation.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LocalOrderRecord - 本地订单记录（从本地数据库提取的对账宽表）
 *
 * 只提取对账关心的字段，通过 outTradeNo 与 ChannelBillRecord 做 JOIN 比对。
 *
 * @author ibqy
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
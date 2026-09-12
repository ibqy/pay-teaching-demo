package com.xb.pay.reconciliation.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 渠道账单记录 — 三方支付平台返回的对账单中的"一行"
 * 无论支付宝还是微信，最终都统一转成这个模型做比对。
 * ---- 对账基础概念 ----
 * 渠道流水号：三方支付平台的交易凭证号（支付宝：trade_no，微信：transaction_id）
 * 商户订单号：我方系统生成的订单号（out_trade_no），是两方关联的"关联键"
 * 交易金额：该笔交易的实际金额，单位：元
 * 交易时间：支付完成时间（含时区，统一转本地时间存储）
 * 交易状态：TRADE_SUCCESS / REFUND_SUCCESS 等
 */
public class ChannelBillRecord {

    private String channel;                 // 渠道标识：ALIPAY / WECHAT
    private String transId;                 // 渠道流水号
    private String outTradeNo;              // 商户订单号（关联键）
    private BigDecimal amount;              // 交易金额（元）
    private LocalDateTime transTime;        // 交易时间
    private String tradeStatus;             // 交易状态
    private String body;                    // 商品描述（可选）

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getTransId() { return transId; }
    public void setTransId(String transId) { this.transId = transId; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getTransTime() { return transTime; }
    public void setTransTime(LocalDateTime transTime) { this.transTime = transTime; }
    public String getTradeStatus() { return tradeStatus; }
    public void setTradeStatus(String tradeStatus) { this.tradeStatus = tradeStatus; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    @Override
    public String toString() {
        return "ChannelBillRecord{" +
                "channel='" + channel + '\'' +
                ", transId='" + transId + '\'' +
                ", outTradeNo='" + outTradeNo + '\'' +
                ", amount=" + amount +
                ", transTime=" + transTime +
                ", tradeStatus='" + tradeStatus + '\'' +
                '}';
    }
}
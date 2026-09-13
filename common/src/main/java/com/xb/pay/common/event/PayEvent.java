package com.xb.pay.common.event;

import java.math.BigDecimal;

public class PayEvent {

    private String channel;
    private String outTradeNo;
    private String tradeNo;
    private BigDecimal amount;
    private String eventType;
    private int retryCount;

    public PayEvent() {}
    public PayEvent(String channel, String outTradeNo, String tradeNo, BigDecimal amount, String eventType) {
        this.channel = channel;
        this.outTradeNo = outTradeNo;
        this.tradeNo = tradeNo;
        this.amount = amount;
        this.eventType = eventType;
    }

    public void incrRetry() { this.retryCount++; }
    public boolean isExhausted(int maxRetries) { return retryCount >= maxRetries; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public String getTradeNo() { return tradeNo; }
    public void setTradeNo(String tradeNo) { this.tradeNo = tradeNo; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    @Override
    public String toString() {
        return "PayEvent{channel='" + channel + "', outTradeNo='" + outTradeNo +
                "', eventType='" + eventType + "', retry=" + retryCount + '}';
    }
}

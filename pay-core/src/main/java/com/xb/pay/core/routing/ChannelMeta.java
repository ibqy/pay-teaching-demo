package com.xb.pay.core.routing;

import com.xb.pay.common.enums.PayChannel;
import java.math.BigDecimal;

public class ChannelMeta {

    private PayChannel channel;
    private BigDecimal feeRate;
    private int weight;
    private boolean enabled;
    private long avgResponseMs;
    private double successRate;

    public ChannelMeta(PayChannel channel, BigDecimal feeRate, int weight, boolean enabled) {
        this.channel = channel;
        this.feeRate = feeRate;
        this.weight = weight;
        this.enabled = enabled;
        this.avgResponseMs = 200;
        this.successRate = 1.0;
    }

    public PayChannel getChannel() { return channel; }
    public BigDecimal getFeeRate() { return feeRate; }
    public int getWeight() { return weight; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public long getAvgResponseMs() { return avgResponseMs; }
    public void setAvgResponseMs(long avgResponseMs) { this.avgResponseMs = avgResponseMs; }
    public double getSuccessRate() { return successRate; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }

    public ChannelMeta setChannel(PayChannel channel) { this.channel = channel; return this; }
    public ChannelMeta setFeeRate(BigDecimal feeRate) { this.feeRate = feeRate; return this; }
    public ChannelMeta setWeight(int weight) { this.weight = weight; return this; }
}

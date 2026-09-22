package com.xb.pay.core.routing;

import com.xb.pay.common.enums.PayChannel;
import java.math.BigDecimal;

/**
 * ChannelMeta - 渠道元数据，用于路由引擎选择最优支付通道
 *
 * 记录每个渠道的费率、权重、启用状态、平均响应时间和成功率。
 * 路由引擎根据这些指标进行加权排序，选出最优渠道。
 *
 * @author ibqy
 */
public class ChannelMeta {

    private PayChannel channel;
    private BigDecimal feeRate;
    private int weight;
    private boolean enabled;
    private long avgResponseMs;
    private double successRate;

    /**
     * 构造渠道元数据
     * @param channel 支付渠道
     * @param feeRate 手续费率
     * @param weight 路由权重（越大优先级越高）
     * @param enabled 是否启用
     */
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

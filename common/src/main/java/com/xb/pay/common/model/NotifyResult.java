package com.xb.pay.common.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * NotifyResult - 异步通知结果（三方支付回调解析后的统一模型）
 *
 * 支付宝和微信的回调参数格式不同，统一解析为此模型。
 * 核心流程：验签 → 解析 → 幂等处理 → 返回 success 告知渠道停止重发。
 *
 * @author ibqy
 */
public class NotifyResult {

    /** 商户订单号 */
    private String outTradeNo;
    /** 三方流水号 */
    private String tradeNo;
    /** 支付金额 */
    private BigDecimal amount;
    /** 买家 ID（支付宝 buyerId / 微信 openId） */
    private String buyerId;
    /** 支付成功时间 */
    private LocalDateTime paidAt;
    /** 原始通知参数（用于日志排查） */
    private String rawParams;
    /** 渠道（支付宝/微信） */
    private String channel;

    public String outTradeNo() { return outTradeNo; }
    public String tradeNo() { return tradeNo; }
    public BigDecimal amount() { return amount; }
    public String buyerId() { return buyerId; }
    public LocalDateTime paidAt() { return paidAt; }
    public String rawParams() { return rawParams; }
    public String channel() { return channel; }

    public void setOutTradeNo(String v) { outTradeNo = v; }
    public void setTradeNo(String v) { tradeNo = v; }
    public void setAmount(BigDecimal v) { amount = v; }
    public void setBuyerId(String v) { buyerId = v; }
    public void setPaidAt(LocalDateTime v) { paidAt = v; }
    public void setRawParams(String v) { rawParams = v; }
    public void setChannel(String v) { channel = v; }
}
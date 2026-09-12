package com.xb.pay.common.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 异步通知结果（三方支付回调解析后的统一模型）
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>知识点</b>：异步通知是支付中最重要的环节。
 * <ul>
 *   <li>支付成功后，支付宝/微信会 POST 通知到商户 notifyUrl</li>
 *   <li>商户收到通知必须验签 → 判断状态 → 处理业务 → 返回成功</li>
 *   <li>收到通知后返回 "success"（微信）/ "success"（支付宝），
 *       否则会重复通知（最多 5 次）</li>
 *   <li>幂等处理：同一个通知可能收到多次，必须用 outTradeNo 去重</li>
 * </ul></p>
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
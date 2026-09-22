package com.xb.pay.common.model;

import com.xb.pay.common.enums.PayChannel;
import com.xb.pay.common.enums.PayMethod;
import com.xb.pay.common.enums.TradeStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PayOrder - 支付订单模型
 *
 * 承载一次支付请求的全部参数，是策略模式的核心传输对象。
 * 业务订单与支付订单一对一设计，tradeNo 是三方平台返回的流水号。
 *
 * @author ibqy
 */
public class PayOrder {

    /** 商户订单号（自己生成的唯一单号） */
    private String outTradeNo;
    /** 支付渠道 */
    private PayChannel channel;
    /** 支付方式 */
    private PayMethod method;
    /** 支付金额（元） */
    private BigDecimal amount;
    /** 商品描述 */
    private String description;
    /** 附加数据（回调时原样返回，用于关联业务） */
    private String attach;
    /** 支付过期时间 */
    private LocalDateTime expireAt;
    /** 三方流水号（支付宝/微信返回的交易号） */
    private String tradeNo;
    /** 交易状态 */
    private TradeStatus status;
    /** 支付成功时间 */
    private LocalDateTime paidAt;
    /** 通知地址 */
    private String notifyUrl;

    public String outTradeNo() { return outTradeNo; }
    public PayChannel channel() { return channel; }
    public PayMethod method() { return method; }
    public BigDecimal amount() { return amount; }
    public String description() { return description; }
    public String attach() { return attach; }
    public LocalDateTime expireAt() { return expireAt; }
    public String tradeNo() { return tradeNo; }
    public TradeStatus status() { return status; }
    public LocalDateTime paidAt() { return paidAt; }
    public String notifyUrl() { return notifyUrl; }

    public void setOutTradeNo(String v) { outTradeNo = v; }
    public void setChannel(PayChannel v) { channel = v; }
    public void setMethod(PayMethod v) { method = v; }
    public void setAmount(BigDecimal v) { amount = v; }
    public void setDescription(String v) { description = v; }
    public void setAttach(String v) { attach = v; }
    public void setExpireAt(LocalDateTime v) { expireAt = v; }
    public void setTradeNo(String v) { tradeNo = v; }
    public void setStatus(TradeStatus v) { status = v; }
    public void setPaidAt(LocalDateTime v) { paidAt = v; }
    public void setNotifyUrl(String v) { notifyUrl = v; }
}
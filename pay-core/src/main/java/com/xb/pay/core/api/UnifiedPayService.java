package com.xb.pay.core.api;

import com.xb.pay.common.model.*;

/**
 * 统一支付接口 —— 策略模式
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>知识点</b>：定义统一的支付抽象接口，各渠道实现（支付宝/微信）通过策略模式注入。
 * 业务层只依赖该接口，不依赖具体 SDK，切换渠道只需替换实现类。
 * 符合 DIP（依赖倒置原则）：抽象不依赖细节，细节依赖抽象。</p>
 *
 * <p><b>生产场景</b>：商户系统同时对接支付宝/微信/银联/PayPal，统一接口 + 策略工厂管理。</p>
 */
public interface UnifiedPayService {

    /** 统一下单 */
    PayResponse placeOrder(PayOrder order);

    /** 查询订单 */
    PayOrder queryOrder(String outTradeNo);

    /** 申请退款 */
    RefundResponse refund(RefundRequest request);

    /** 关闭订单 */
    boolean closeOrder(String outTradeNo);

    /** 解析异步通知 */
    NotifyResult parseNotify(String rawBody, String signature, String channel);
}
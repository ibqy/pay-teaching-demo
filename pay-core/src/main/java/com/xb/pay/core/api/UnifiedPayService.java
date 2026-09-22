package com.xb.pay.core.api;

import com.xb.pay.common.enums.PayChannel;
import com.xb.pay.common.model.*;

/**
 * UnifiedPayService - 统一支付接口（策略模式）
 *
 * 定义下单、查询、退款、关单、解析通知五大操作，
 * 各渠道（支付宝/微信）通过实现类注入，业务层只依赖此接口，符合 DIP 原则。
 *
 * @author ibqy
 */
public interface UnifiedPayService {

    /** 返回当前实现对应的支付渠道 */
    PayChannel channel();

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
package com.xb.pay.demo.controller;

import com.xb.pay.common.enums.PayChannel;
import com.xb.pay.common.enums.PayMethod;
import com.xb.pay.common.model.*;
import com.xb.pay.common.util.OrderNoGenerator;
import com.xb.pay.core.strategy.PayStrategyFactory;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付演示 Controller
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>知识点</b>：演示支付全流程：
 * <ol>
 *   <li>下单 → 获取支付参数 → 用户扫码/跳转</li>
 *   <li>支付成功 → 异步通知（notify）→ 更新订单状态</li>
 *   <li>查询 → 确认交易结果</li>
 *   <li>退款 → 用户维权/取消订单</li>
 *   <li>关单 → 超时未支付取消</li>
 * </ol></p>
 */
@RestController
@RequestMapping("/api")
public class PayDemoController {

    private final PayStrategyFactory payFactory;

    public PayDemoController(PayStrategyFactory payFactory) {
        this.payFactory = payFactory;
    }

    /**
     * 统一下单
     * @param channel  ALIPAY / WECHAT
     * @param method   NATIVE / JSAPI / H5 / APP
     */
    @PostMapping("/pay/{channel}/{method}")
    public PayResponse pay(
            @PathVariable PayChannel channel,
            @PathVariable PayMethod method,
            @RequestParam(defaultValue = "0.01") BigDecimal amount,
            @RequestParam(defaultValue = "测试商品") String description) {

        var order = new PayOrder();
        order.setOutTradeNo(OrderNoGenerator.next()); // 生成商户订单号
        order.setChannel(channel);
        order.setMethod(method);
        order.setAmount(amount);
        order.setDescription(description);
        order.setExpireAt(LocalDateTime.now().plusMinutes(30)); // 30分钟过期

        return payFactory.get(channel).placeOrder(order);
    }

    /** 查询订单 */
    @GetMapping("/query/{channel}/{outTradeNo}")
    public PayOrder query(@PathVariable PayChannel channel, @PathVariable String outTradeNo) {
        return payFactory.get(channel).queryOrder(outTradeNo);
    }

    /** 退款 */
    @PostMapping("/refund/{channel}")
    public RefundResponse refund(@PathVariable PayChannel channel, @RequestBody RefundRequest request) {
        return payFactory.get(channel).refund(request);
    }

    /** 关单 */
    @PostMapping("/close/{channel}/{outTradeNo}")
    public String close(@PathVariable PayChannel channel, @PathVariable String outTradeNo) {
        boolean ok = payFactory.get(channel).closeOrder(outTradeNo);
        return ok ? "关单成功" : "关单失败";
    }

    /** 异步通知接收（支付宝） */
    @PostMapping("/notify/alipay")
    public String alipayNotify(@RequestBody String body) {
        var svc = payFactory.get(PayChannel.ALIPAY);
        NotifyResult result = svc.parseNotify(body, null, "alipay");
        // TODO: 处理业务（更新订单状态、发送消息等）
        return "success";
    }

    /** 异步通知接收（微信） */
    @PostMapping("/notify/wechat")
    public String wechatNotify(@RequestBody String body,
                               @RequestHeader("Wechatpay-Signature") String signature) {
        var svc = payFactory.get(PayChannel.WECHAT);
        NotifyResult result = svc.parseNotify(body, signature, "wechat");
        return "SUCCESS";
    }
}
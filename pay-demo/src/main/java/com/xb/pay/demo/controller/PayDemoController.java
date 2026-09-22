package com.xb.pay.demo.controller;

import com.xb.pay.common.enums.PayChannel;
import com.xb.pay.common.enums.PayMethod;
import com.xb.pay.common.enums.TradeStatus;
import com.xb.pay.common.model.*;
import com.xb.pay.common.util.OrderNoGenerator;
import com.xb.pay.core.strategy.PayStrategyFactory;
import com.xb.pay.demo.db.entity.PayOrderEntity;
import com.xb.pay.demo.db.repository.PayOrderRepository;
import com.xb.pay.demo.event.DeadLetterQueue;
import com.xb.pay.demo.service.NotifyService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * PayDemoController - 支付演示控制器，暴露下单/查询/退款/关单/通知等 REST 接口
 *
 * 演示完整支付流程：创建订单 → 调用三方下单 → 异步通知处理 → 回调解析。
 * 包含自动路由（/pay/auto）和手动指定渠道（/pay/{channel}）两种下单方式。
 *
 * @author ibqy
 */
@RestController
@RequestMapping("/api")
public class PayDemoController {

    private final PayStrategyFactory payFactory;
    private final PayOrderRepository orderRepo;
    private final DeadLetterQueue deadLetterQueue;
    private final NotifyService notifyService;

    public PayDemoController(PayStrategyFactory payFactory, PayOrderRepository orderRepo,
                             DeadLetterQueue deadLetterQueue,
                             NotifyService notifyService) {
        this.payFactory = payFactory;
        this.orderRepo = orderRepo;
        this.deadLetterQueue = deadLetterQueue;
        this.notifyService = notifyService;
    }

    /**
     * 自动路由下单：根据支付方式自动选择最优渠道
     * @param method 支付方式（NATIVE/H5/APP 等）
     * @param amount 支付金额，默认 0.01 元
     * @param description 商品描述
     * @return 支付响应（二维码 URL / 表单 HTML / 唤起参数）
     */
    @PostMapping("/pay/auto/{method}")
    public PayResponse payAuto(@PathVariable PayMethod method,
                               @RequestParam(defaultValue = "0.01") BigDecimal amount,
                               @RequestParam(defaultValue = "商品") String description) {
        var svc = payFactory.route(method);
        String outTradeNo = OrderNoGenerator.next();

        var e = new PayOrderEntity();
        e.setOutTradeNo(outTradeNo);
        e.setChannel(svc.channel().name());
        e.setMethod(method.name());
        e.setAmount(amount);
        e.setDescription(description);
        e.setExpireAt(LocalDateTime.now().plusMinutes(30));
        e.setStatus(TradeStatus.WAITING.name());
        orderRepo.save(e);

        var order = new PayOrder()
                .setOutTradeNo(outTradeNo)
                .setChannel(svc.channel())
                .setMethod(method)
                .setAmount(amount)
                .setDescription(description)
                .setExpireAt(LocalDateTime.now().plusMinutes(30));
        return svc.placeOrder(order);
    }

    /**
     * 手动指定渠道下单：直接使用指定渠道的 SDK
     * @param channel 支付渠道（ALIPAY/WECHAT）
     * @param method 支付方式
     * @param amount 支付金额
     * @param description 商品描述
     * @return 支付响应
     */
    @PostMapping("/pay/{channel}/{method}")
    public PayResponse pay(@PathVariable PayChannel channel, @PathVariable PayMethod method,
                           @RequestParam(defaultValue = "0.01") BigDecimal amount,
                           @RequestParam(defaultValue = "商品") String description) {
        var order = new PayOrder()
                .setOutTradeNo(OrderNoGenerator.next())
                .setChannel(channel)
                .setMethod(method)
                .setAmount(amount)
                .setDescription(description)
                .setExpireAt(LocalDateTime.now().plusMinutes(30));
        saveOrder(order);
        return payFactory.get(channel).placeOrder(order);
    }

    private void saveOrder(PayOrder order) {
        var e = new PayOrderEntity();
        e.setOutTradeNo(order.outTradeNo());
        e.setChannel(order.channel() != null ? order.channel().name() : null);
        e.setMethod(order.method() != null ? order.method().name() : null);
        e.setAmount(order.amount());
        e.setDescription(order.description());
        e.setExpireAt(order.expireAt());
        e.setStatus(TradeStatus.WAITING.name());
        orderRepo.save(e);
    }

    /**
     * 查询订单状态
     * @param outTradeNo 商户订单号
     * @return 订单实体
     */
    @GetMapping("/query/{outTradeNo}")
    public PayOrderEntity query(@PathVariable String outTradeNo) {
        return orderRepo.findByOutTradeNo(outTradeNo)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + outTradeNo));
    }

    /**
     * 申请退款
     * @param channel 支付渠道
     * @param request 退款请求（原订单号 + 退款金额 + 原因）
     * @return 退款响应
     */
    @PostMapping("/refund/{channel}")
    public RefundResponse refund(@PathVariable PayChannel channel, @RequestBody RefundRequest request) {
        return payFactory.get(channel).refund(request);
    }

    /**
     * 关闭订单（超时未支付的订单可主动关单）
     * @param channel 支付渠道
     * @param outTradeNo 商户订单号
     * @return 关单结果描述
     */
    @PostMapping("/close/{channel}/{outTradeNo}")
    public String close(@PathVariable PayChannel channel, @PathVariable String outTradeNo) {
        boolean ok = payFactory.get(channel).closeOrder(outTradeNo);
        return ok ? "关单成功" : "关单失败";
    }

    /**
     * 支付宝异步通知回调入口
     * @param body 通知原始表单参数
     * @return "success" 告知支付宝停止重发
     */
    @PostMapping("/notify/alipay")
    public String alipayNotify(@RequestBody String body) {
        var svc = payFactory.get(PayChannel.ALIPAY);
        NotifyResult result = svc.parseNotify(body, null, "alipay");
        String outTradeNo = result.outTradeNo() != null ? result.outTradeNo() : "NOTIFY_DEMO";
        notifyService.handleNotify(outTradeNo, result, "ALIPAY");
        return "success";
    }

    /**
     * 微信支付异步通知回调入口
     * @param body 加密的通知 JSON
     * @param signature 微信签名（Wechatpay-Signature 请求头）
     * @return "SUCCESS" 告知微信停止重发
     */
    @PostMapping("/notify/wechat")
    public String wechatNotify(@RequestBody String body,
                               @RequestHeader("Wechatpay-Signature") String signature) {
        var svc = payFactory.get(PayChannel.WECHAT);
        NotifyResult result = svc.parseNotify(body, signature, "wechat");
        String outTradeNo = result.outTradeNo() != null ? result.outTradeNo() : "NOTIFY_DEMO";
        notifyService.handleNotify(outTradeNo, result, "WECHAT");
        return "SUCCESS";
    }

    /**
     * 查看死信队列中的所有失败事件
     * @return 死信列表
     */
    @GetMapping("/dlq")
    public List<DeadLetterQueue.DeadLetter> dlq() {
        return deadLetterQueue.getAll();
    }
}

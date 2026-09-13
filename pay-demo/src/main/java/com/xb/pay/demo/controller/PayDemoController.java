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

    @GetMapping("/query/{outTradeNo}")
    public PayOrderEntity query(@PathVariable String outTradeNo) {
        return orderRepo.findByOutTradeNo(outTradeNo)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + outTradeNo));
    }

    @PostMapping("/refund/{channel}")
    public RefundResponse refund(@PathVariable PayChannel channel, @RequestBody RefundRequest request) {
        return payFactory.get(channel).refund(request);
    }

    @PostMapping("/close/{channel}/{outTradeNo}")
    public String close(@PathVariable PayChannel channel, @PathVariable String outTradeNo) {
        boolean ok = payFactory.get(channel).closeOrder(outTradeNo);
        return ok ? "关单成功" : "关单失败";
    }

    @PostMapping("/notify/alipay")
    public String alipayNotify(@RequestBody String body) {
        var svc = payFactory.get(PayChannel.ALIPAY);
        NotifyResult result = svc.parseNotify(body, null, "alipay");
        String outTradeNo = result.outTradeNo() != null ? result.outTradeNo() : "NOTIFY_DEMO";
        notifyService.handleNotify(outTradeNo, result, "ALIPAY");
        return "success";
    }

    @PostMapping("/notify/wechat")
    public String wechatNotify(@RequestBody String body,
                               @RequestHeader("Wechatpay-Signature") String signature) {
        var svc = payFactory.get(PayChannel.WECHAT);
        NotifyResult result = svc.parseNotify(body, signature, "wechat");
        String outTradeNo = result.outTradeNo() != null ? result.outTradeNo() : "NOTIFY_DEMO";
        notifyService.handleNotify(outTradeNo, result, "WECHAT");
        return "SUCCESS";
    }

    @GetMapping("/dlq")
    public List<DeadLetterQueue.DeadLetter> dlq() {
        return deadLetterQueue.getAll();
    }
}

package com.xb.pay.demo.service;

import com.xb.pay.common.annotation.Idempotent;
import com.xb.pay.common.enums.TradeStatus;
import com.xb.pay.common.event.PayEvent;
import com.xb.pay.common.model.NotifyResult;
import com.xb.pay.demo.db.entity.PayOrderEntity;
import com.xb.pay.demo.db.repository.PayOrderRepository;
import com.xb.pay.demo.event.PayEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * NotifyService - 异步通知处理服务，支付回调的核心业务逻辑
 *
 * 接收三方通知后执行：幂等校验 → 金额核对 → 更新订单 → 发布事件。
 * 使用 @Idempotent 防重复处理，@Transactional 保证事务一致性。
 *
 * @author ibqy
 */
@Service
public class NotifyService {

    private static final Logger log = LoggerFactory.getLogger(NotifyService.class);
    private final PayOrderRepository orderRepo;
    private final PayEventPublisher eventPublisher;

    public NotifyService(PayOrderRepository orderRepo, PayEventPublisher eventPublisher) {
        this.orderRepo = orderRepo;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 处理三方支付异步通知：幂等保护 → 金额校验 → 更新订单 → 发布事件
     * @param outTradeNo 商户订单号
     * @param result 解析后的通知结果
     * @param channel 支付渠道标识
     */
    @Idempotent(key = "#outTradeNo", ttl = 600)
    @Transactional
    public void handleNotify(String outTradeNo, NotifyResult result, String channel) {
        log.info("处理通知: outTradeNo={}, channel={}", outTradeNo, channel);

        orderRepo.findByOutTradeNo(outTradeNo).ifPresentOrElse(entity -> {
            // 幂等保护：订单已成功则跳过，避免重复处理
            if (TradeStatus.SUCCESS.name().equals(entity.getStatus())) {
                log.info("订单已成功，跳过重复通知: {}", outTradeNo);
                return;
            }
            // 金额校验：通知金额与下单金额不一致时记录告警
            if (result.amount() != null && result.amount().compareTo(entity.getAmount()) != 0) {
                log.warn("通知金额与订单金额不一致: outTradeNo={}, 订单金额={}, 通知金额={}",
                        outTradeNo, entity.getAmount(), result.amount());
            }
            entity.setStatus(TradeStatus.SUCCESS.name());
            if (result.tradeNo() != null) entity.setTradeNo(result.tradeNo());
            entity.setPaidAt(LocalDateTime.now());
            orderRepo.save(entity);
            log.info("订单更新: {}", outTradeNo);
        }, () -> {
            // TODO: 此处为通知先于下单的场景，金额应与原始订单进行校验
            var e = new PayOrderEntity();
            e.setOutTradeNo(outTradeNo);
            e.setStatus(TradeStatus.SUCCESS.name());
            e.setTradeNo(result.tradeNo());
            e.setAmount(result.amount());
            e.setPaidAt(LocalDateTime.now());
            orderRepo.save(e);
            log.info("订单创建(通知先于下单): {}", outTradeNo);
        });

        eventPublisher.publish(new PayEvent(channel, outTradeNo,
                result.tradeNo(), result.amount(), "PAY_SUCCESS"));
    }
}

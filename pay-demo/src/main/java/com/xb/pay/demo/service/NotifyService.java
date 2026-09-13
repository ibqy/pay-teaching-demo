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

@Service
public class NotifyService {

    private static final Logger log = LoggerFactory.getLogger(NotifyService.class);
    private final PayOrderRepository orderRepo;
    private final PayEventPublisher eventPublisher;

    public NotifyService(PayOrderRepository orderRepo, PayEventPublisher eventPublisher) {
        this.orderRepo = orderRepo;
        this.eventPublisher = eventPublisher;
    }

    @Idempotent(key = "#outTradeNo", ttl = 600)
    @Transactional
    public void handleNotify(String outTradeNo, NotifyResult result, String channel) {
        log.info("处理通知: outTradeNo={}, channel={}", outTradeNo, channel);

        orderRepo.findByOutTradeNo(outTradeNo).ifPresentOrElse(entity -> {
            entity.setStatus(TradeStatus.SUCCESS.name());
            if (result.tradeNo() != null) entity.setTradeNo(result.tradeNo());
            if (result.amount() != null) entity.setAmount(result.amount());
            entity.setPaidAt(LocalDateTime.now());
            orderRepo.save(entity);
            log.info("订单更新: {}", outTradeNo);
        }, () -> {
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

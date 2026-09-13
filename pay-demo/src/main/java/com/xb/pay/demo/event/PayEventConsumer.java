package com.xb.pay.demo.event;

import com.xb.pay.common.enums.TradeStatus;
import com.xb.pay.common.event.PayEvent;
import com.xb.pay.demo.db.entity.PayOrderEntity;
import com.xb.pay.demo.db.repository.PayOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PayEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PayEventConsumer.class);
    private static final int MAX_RETRIES = 3;

    private final PayOrderRepository orderRepo;
    private final DeadLetterQueue deadLetterQueue;

    public PayEventConsumer(PayOrderRepository orderRepo, DeadLetterQueue deadLetterQueue) {
        this.orderRepo = orderRepo;
        this.deadLetterQueue = deadLetterQueue;
    }

    @Async
    @EventListener
    public void handlePayEvent(PayEvent event) {
        try {
            log.info("消费事件: {}", event);

            if ("PAY_SUCCESS".equals(event.getEventType())) {
                PayOrderEntity entity = orderRepo.findByOutTradeNo(event.getOutTradeNo())
                        .orElseThrow(() -> new RuntimeException("订单不存在: " + event.getOutTradeNo()));
                entity.setStatus(TradeStatus.SUCCESS.name());
                entity.setTradeNo(event.getTradeNo());
                entity.setPaidAt(LocalDateTime.now());
                orderRepo.save(entity);
                log.info("订单更新成功: {}", event.getOutTradeNo());
            }
        } catch (Exception e) {
            event.incrRetry();
            log.warn("消费失败(outTradeNo={}), 重试 {}/{}: {}",
                    event.getOutTradeNo(), event.getRetryCount(), MAX_RETRIES, e.getMessage());
            if (event.isExhausted(MAX_RETRIES)) {
                deadLetterQueue.push(event, e.getMessage());
            } else {
                handlePayEvent(event);
            }
        }
    }
}

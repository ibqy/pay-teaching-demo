package com.xb.pay.demo.event;

import com.xb.pay.common.event.PayEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * DeadLetterQueue - 死信队列，存储重试耗尽后仍失败的支付事件
 *
 * 生产环境通常对接 RabbitMQ/Kafka 死信交换机，
 * 此处用内存 List 演示，便于通过 REST 接口查看失败事件。
 *
 * @author ibqy
 */
@Component
public class DeadLetterQueue {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueue.class);
    private final List<DeadLetter> letters = new ArrayList<>();

    /**
     * 将重试耗尽的事件推入死信队列
     * @param event 失败的支付事件
     * @param reason 失败原因
     */
    public void push(PayEvent event, String reason) {
        DeadLetter letter = new DeadLetter(event.getOutTradeNo(), event.getChannel(),
                event.getEventType(), event.getRetryCount(), reason, System.currentTimeMillis());
        letters.add(letter);
        log.error("事件进入死信队列: outTradeNo={}, cause={}", event.getOutTradeNo(), reason);
    }

    public List<DeadLetter> getAll() { return new ArrayList<>(letters); }
    public int size() { return letters.size(); }

    public record DeadLetter(String outTradeNo, String channel, String eventType,
                             int retryCount, String reason, long timestamp) {}
}

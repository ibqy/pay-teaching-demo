package com.xb.pay.demo.event;

import com.xb.pay.common.event.PayEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * PayEventPublisher - 支付事件发布者，封装 Spring ApplicationEventPublisher
 *
 * 通知处理完成后调用此组件发布事件，解耦事件产生与消费。
 *
 * @author ibqy
 */
@Component
public class PayEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PayEventPublisher.class);
    private final ApplicationEventPublisher publisher;

    public PayEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * 发布支付事件到 Spring 事件总线
     * @param event 待发布的支付事件
     */
    public void publish(PayEvent event) {
        log.info("发布事件: {}", event);
        publisher.publishEvent(event);
    }
}

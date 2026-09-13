package com.xb.pay.demo.event;

import com.xb.pay.common.event.PayEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class PayEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PayEventPublisher.class);
    private final ApplicationEventPublisher publisher;

    public PayEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(PayEvent event) {
        log.info("发布事件: {}", event);
        publisher.publishEvent(event);
    }
}

package com.xb.pay.core.strategy;

import com.xb.pay.common.enums.PayChannel;
import com.xb.pay.common.enums.PayMethod;
import com.xb.pay.core.api.UnifiedPayService;
import com.xb.pay.core.routing.ChannelMeta;
import com.xb.pay.core.routing.RoutingEngine;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PayStrategyFactory {

    private static final Logger log = LoggerFactory.getLogger(PayStrategyFactory.class);
    private final Map<PayChannel, UnifiedPayService> services = new EnumMap<>(PayChannel.class);
    private RoutingEngine routingEngine;

    public PayStrategyFactory(List<UnifiedPayService> serviceList) {
        for (var svc : serviceList) {
            services.put(svc.channel(), svc);
        }
    }

    @PostConstruct
    public void initRouting() {
        List<ChannelMeta> metas = List.of(
                new ChannelMeta(PayChannel.ALIPAY, new BigDecimal("0.006"), 10, true),
                new ChannelMeta(PayChannel.WECHAT, new BigDecimal("0.006"), 10, true)
        );
        this.routingEngine = new RoutingEngine(metas);
        log.info("路由引擎初始化完成, channels={}", metas.size());
    }

    public UnifiedPayService get(PayChannel channel) {
        var svc = services.get(channel);
        if (svc == null) {
            throw new IllegalArgumentException("不支持的渠道: " + channel);
        }
        return svc;
    }

    public UnifiedPayService route(PayMethod method) {
        PayChannel channel = routingEngine.route(method);
        if (channel == null) {
            throw new IllegalStateException("无可用的支付渠道, method=" + method);
        }
        log.info("路由选择: method={} → channel={}", method, channel);
        return get(channel);
    }

    public RoutingEngine getRoutingEngine() { return routingEngine; }
}

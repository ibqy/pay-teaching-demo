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

/**
 * PayStrategyFactory - 支付策略工厂，管理所有渠道实现并提供路由能力
 *
 * 利用 Spring 自动注入所有 UnifiedPayService 实现，按渠道注册到 Map 中。
 * 结合 RoutingEngine 实现智能路由：业务层只需传入支付方式，自动选择渠道。
 *
 * @author ibqy
 */
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

    /**
     * 按指定渠道获取支付服务实现
     * @param channel 支付渠道
     * @return 对应的 UnifiedPayService 实现
     * @throws IllegalArgumentException 渠道不支持时抛出
     */
    public UnifiedPayService get(PayChannel channel) {
        var svc = services.get(channel);
        if (svc == null) {
            throw new IllegalArgumentException("不支持的渠道: " + channel);
        }
        return svc;
    }

    /**
     * 根据支付方式自动路由，返回最优渠道的支付服务
     * @param method 支付方式
     * @return 路由选中的支付服务实现
     * @throws IllegalStateException 无可用渠道时抛出
     */
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

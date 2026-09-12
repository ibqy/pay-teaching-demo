package com.xb.pay.core.strategy;

import com.xb.pay.common.enums.PayChannel;
import com.xb.pay.core.api.UnifiedPayService;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 支付策略工厂
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>知识点</b>：通过工厂模式管理多个支付渠道实现类。
 * Spring 启动时自动收集所有 UnifiedPayService 实现，
 * 业务方通过 channel 选择即可，无需关心具体实现类名称。</p>
 */
@Component
public class PayStrategyFactory {

    private final Map<PayChannel, UnifiedPayService> services = new EnumMap<>(PayChannel.class);

    public PayStrategyFactory(List<UnifiedPayService> serviceList) {
        for (var svc : serviceList) {
            services.put(svc.channel(), svc);
        }
    }

    /** 根据渠道获取支付服务 */
    public UnifiedPayService get(PayChannel channel) {
        var svc = services.get(channel);
        if (svc == null) {
            throw new IllegalArgumentException("不支持的支付渠道：" + channel);
        }
        return svc;
    }
}
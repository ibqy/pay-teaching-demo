package com.xb.pay.core.routing;

import com.xb.pay.common.enums.PayChannel;
import com.xb.pay.common.enums.PayMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RoutingEngine {

    private static final Logger log = LoggerFactory.getLogger(RoutingEngine.class);
    private final List<ChannelMeta> channels;

    public RoutingEngine(List<ChannelMeta> channels) {
        this.channels = new ArrayList<>(channels);
    }

    public PayChannel route(PayMethod method) {
        List<ChannelMeta> candidates = channels.stream()
                .filter(ChannelMeta::isEnabled)
                .filter(c -> supports(c.getChannel(), method))
                .sorted(Comparator
                        .comparingInt(ChannelMeta::getWeight).reversed()
                        .thenComparing(ChannelMeta::getFeeRate))
                .toList();

        if (candidates.isEmpty()) {
            log.warn("无可用渠道, method={}", method);
            return null;
        }

        ChannelMeta best = candidates.get(0);
        log.info("路由结果: {} → {}, weight={}, feeRate={}",
                method, best.getChannel(), best.getWeight(), best.getFeeRate());
        return best.getChannel();
    }

    private boolean supports(PayChannel channel, PayMethod method) {
        return switch (channel) {
            case ALIPAY -> method != PayMethod.JSAPI;
            case WECHAT -> method == PayMethod.NATIVE || method == PayMethod.JSAPI || method == PayMethod.H5;
        };
    }

    public List<ChannelMeta> getChannels() { return channels; }
}

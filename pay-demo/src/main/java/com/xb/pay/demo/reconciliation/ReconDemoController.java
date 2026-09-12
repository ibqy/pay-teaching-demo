package com.xb.pay.demo.reconciliation;

import com.xb.pay.reconciliation.model.ReconReport;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 对账演示 Controller — 手动触发对账
 * <p>GET /api/recon/alipay — 执行支付宝对账演示</p>
 * <p>GET /api/recon/wechat — 执行微信对账演示</p>
 */
@RestController
@RequestMapping("/api/recon")
public class ReconDemoController {

    private final ReconDemoService reconService;

    public ReconDemoController(ReconDemoService reconService) {
        this.reconService = reconService;
    }

    @GetMapping("/alipay")
    public Map<String, Object> alipay() {
        ReconReport report = reconService.reconcileAlipay();
        return toResult(report);
    }

    @GetMapping("/wechat")
    public Map<String, Object> wechat() {
        ReconReport report = reconService.reconcileWechat();
        return toResult(report);
    }

    private Map<String, Object> toResult(ReconReport r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("channel", r.getChannel());
        m.put("billDate", r.getBillDate());
        m.put("pass", r.isPassed());
        m.put("totalChannel", r.getTotalChannelCount());
        m.put("totalLocal", r.getTotalLocalCount());
        m.put("matched", r.getMatchedCount());
        m.put("diffCount", r.getDiffCount());
        m.put("diffs", r.getDiffs());
        return m;
    }
}
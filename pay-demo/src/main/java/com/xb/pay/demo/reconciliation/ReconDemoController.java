package com.xb.pay.demo.reconciliation;

import com.xb.pay.reconciliation.model.ReconReport;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ReconDemoController - 对账演示控制器，提供手动触发对账的 REST 接口
 *
 * GET /api/recon/alipay 和 /api/recon/wechat 分别触发对应渠道的对账流程，
 * 返回结构化的对账报告供前端展示。
 *
 * @author ibqy
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
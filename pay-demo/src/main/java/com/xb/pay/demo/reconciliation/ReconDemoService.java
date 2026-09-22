package com.xb.pay.demo.reconciliation;

import com.xb.pay.reconciliation.downloader.BillDownloader;
import com.xb.pay.reconciliation.engine.ReconciliationEngine;
import com.xb.pay.reconciliation.model.LocalOrderRecord;
import com.xb.pay.reconciliation.model.ReconReport;
import com.xb.pay.reconciliation.parser.AlipayCsvParser;
import com.xb.pay.reconciliation.parser.BillParser;
import com.xb.pay.reconciliation.parser.WechatCsvParser;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ReconDemoService - 对账演示服务，组装下载→解析→比对的完整链路
 *
 * 真实场景中 queryLocalOrders 应查数据库，此处硬编码模拟数据以演示差异检测。
 * 包含匹配、金额不一致、短款等典型场景。
 *
 * @author ibqy
 */
@Service
public class ReconDemoService {

    private final BillDownloader downloader;

    public ReconDemoService(BillDownloader downloader) {
        this.downloader = downloader;
    }

    /**
     * 执行支付宝对账演示
     */
    public ReconReport reconcileAlipay() {
        BillParser parser = new AlipayCsvParser();
        ReconciliationEngine engine = new ReconciliationEngine(
                downloader, parser, "ALIPAY", "2026-09-11");
        return engine.reconcile(queryLocalOrders("ALIPAY"));
    }

    /**
     * 执行微信支付对账演示
     */
    public ReconReport reconcileWechat() {
        BillParser parser = new WechatCsvParser();
        ReconciliationEngine engine = new ReconciliationEngine(
                downloader, parser, "WECHAT", "2026-09-11");
        return engine.reconcile(queryLocalOrders("WECHAT"));
    }

    /**
     * 模拟本地订单查询 — 包含一些差异数据以演示差异检测
     * <ul>
     *   <li>3 条匹配记录（金额、时间一致）</li>
     *   <li>1 条金额不一致（AMOUNT_MISMATCH）</li>
     *   <li>1 条本地有、渠道无（SHORT_LONG / 短款）</li>
     * </ul>
     */
    private List<LocalOrderRecord> queryLocalOrders(String channel) {
        List<LocalOrderRecord> list = new ArrayList<>();
        LocalDateTime base = LocalDateTime.of(2026, 9, 11, 10, 0);

        // 3 条匹配记录
        list.add(make("OUT202609110001", "49.90", base.plusMinutes(30), "PAID", "202609112200100001"));
        list.add(make("OUT202609110002", "99.00", base.plusMinutes(60), "PAID", "202609112200100002"));
        list.add(make("OUT202609110003", "19.90", base.plusMinutes(135), "PAID", "202609112200100003"));

        // 1 条金额不一致：渠道 200.00，本地 180.00
        list.add(make("OUT202609110004", "180.00", base.plusMinutes(240), "PAID", "202609112200100004"));

        // 1 条短款：渠道无此单
        list.add(make("OUT202609119999", "88.00", base.plusMinutes(300), "PAID", "CH999999999"));

        return list;
    }

    private LocalOrderRecord make(String outTradeNo, String amount,
                                   LocalDateTime time, String status, String transId) {
        LocalOrderRecord r = new LocalOrderRecord();
        r.setOutTradeNo(outTradeNo);
        r.setPayAmount(new BigDecimal(amount));
        r.setPayTime(time);
        r.setTradeStatus(status);
        r.setChannelTransId(transId);
        return r;
    }
}
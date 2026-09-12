package com.xb.pay.reconciliation.engine;

import com.xb.pay.reconciliation.downloader.BillDownloader;
import com.xb.pay.reconciliation.model.*;
import com.xb.pay.reconciliation.parser.BillParser;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 对账引擎 — 核心业务逻辑
 * <p>
 * ---- 完整对账链路 ----
 * ① 下载渠道对账单文件（BillDownloader）
 * ② 解析为标准化记录（BillParser → List{@code <ChannelBillRecord>}）
 * ③ 从本地数据库查询对应日期的支付订单（调用方传入）
 * ④ 以 outTradeNo 为关联键做 FULL OUTER JOIN 比对
 * ⑤ 生成 ReconReport（一致统计 + 差异明细）
 * <p>
 * ---- 差异类型判断规则 ----
 * | 场景                                | 差异类型         | 说明               |
 * |-------------------------------------|-----------------|--------------------|
 * | 渠道有记录，本地 Map 中找不到         | LONG_SHORT      | 长款：渠道单边账    |
 * | 本地有记录，渠道 Map 中找不到         | SHORT_LONG      | 短款：渠道漏回调    |
 * | 双方都有，但渠道金额 ≠ 本地金额       | AMOUNT_MISMATCH | 金额不一致          |
 * | 双方都有，金额一致，但支付时间差 > 5min | TIME_MISMATCH   | 时间偏差大（可告警） |
 */
public class ReconciliationEngine {

    /** 时间偏差阈值：超过该秒数即标记 TIME_MISMATCH */
    private static final long TIME_THRESHOLD_SECONDS = 300; // 5 分钟

    private final BillDownloader downloader;
    private final BillParser parser;
    private final String channel;
    private final String billDate;

    public ReconciliationEngine(BillDownloader downloader, BillParser parser,
                                String channel, String billDate) {
        this.downloader = downloader;
        this.parser = parser;
        this.channel = channel;
        this.billDate = billDate;
    }

    /**
     * 执行对账，返回报告
     *
     * @param localRecords 本地订单记录列表（调用方从数据库查询并按日期过滤）
     * @return 对账报告
     */
    public ReconReport reconcile(List<LocalOrderRecord> localRecords) {
        // ① 下载账单
        InputStream billStream = downloader.download(channel, billDate);

        // ② 解析账单
        List<ChannelBillRecord> channelRecords = parser.parse(billStream);

        // ③ 构建本地订单 Map（关联键：outTradeNo）
        Map<String, LocalOrderRecord> localMap = new HashMap<>();
        for (LocalOrderRecord r : localRecords) {
            localMap.put(r.getOutTradeNo(), r);
        }

        // ④ 比对
        List<ReconDiff> diffs = new ArrayList<>();
        int matchedCount = 0;
        BigDecimal totalChannelAmount = BigDecimal.ZERO;
        BigDecimal totalLocalAmount = BigDecimal.ZERO;

        // ---- 渠道侧遍历：匹配 + 长款检测 ----
        Set<String> matchedKeys = new HashSet<>();
        for (ChannelBillRecord cr : channelRecords) {
            totalChannelAmount = totalChannelAmount.add(cr.getAmount());
            LocalOrderRecord lr = localMap.get(cr.getOutTradeNo());

            if (lr == null) {
                // 渠道有、本地无 → 长款
                diffs.add(buildDiff(ReconDiff.DiffType.LONG_SHORT, cr, null, "渠道有记录，本地不存在"));
            } else {
                matchedKeys.add(cr.getOutTradeNo());
                // 先判断金额
                if (cr.getAmount().compareTo(lr.getPayAmount()) != 0) {
                    diffs.add(buildDiff(ReconDiff.DiffType.AMOUNT_MISMATCH, cr, lr,
                            "金额不一致：渠道=" + cr.getAmount() + ", 本地=" + lr.getPayAmount()));
                } else if (timeDiffExceeds(cr.getTransTime(), lr.getPayTime())) {
                    diffs.add(buildDiff(ReconDiff.DiffType.TIME_MISMATCH, cr, lr,
                            "时间偏差超过 " + TIME_THRESHOLD_SECONDS + " 秒"));
                } else {
                    matchedCount++;
                }
            }
        }

        // ---- 本地侧遍历：短款检测 ----
        for (LocalOrderRecord lr : localRecords) {
            totalLocalAmount = totalLocalAmount.add(lr.getPayAmount());
            if (!matchedKeys.contains(lr.getOutTradeNo())) {
                diffs.add(buildDiff(ReconDiff.DiffType.SHORT_LONG, null, lr, "本地有记录，渠道不存在"));
            }
        }

        // ⑤ 组装报告
        ReconReport report = new ReconReport();
        report.setChannel(channel);
        report.setBillDate(billDate);
        report.setTotalChannelCount(channelRecords.size());
        report.setTotalLocalCount(localRecords.size());
        report.setMatchedCount(matchedCount);
        report.setTotalChannelAmount(totalChannelAmount);
        report.setTotalLocalAmount(totalLocalAmount);
        report.setDiffs(diffs);
        return report;
    }

    /** 判断时间差是否超过阈值 */
    private boolean timeDiffExceeds(LocalDateTime t1, LocalDateTime t2) {
        return Math.abs(ChronoUnit.SECONDS.between(t1, t2)) > TIME_THRESHOLD_SECONDS;
    }

    /** 构建差异对象 */
    private ReconDiff buildDiff(ReconDiff.DiffType type, ChannelBillRecord cr, LocalOrderRecord lr, String remark) {
        ReconDiff diff = new ReconDiff();
        diff.setDiffType(type);
        diff.setOutTradeNo(cr != null ? cr.getOutTradeNo() : (lr != null ? lr.getOutTradeNo() : ""));
        diff.setChannelAmount(cr != null ? cr.getAmount() : BigDecimal.ZERO);
        diff.setLocalAmount(lr != null ? lr.getPayAmount() : BigDecimal.ZERO);
        diff.setChannelTransId(cr != null ? cr.getTransId() : "");
        diff.setLocalStatus(lr != null ? lr.getTradeStatus() : "");
        diff.setChannelStatus(cr != null ? cr.getTradeStatus() : "");
        diff.setRemark(remark);
        return diff;
    }
}
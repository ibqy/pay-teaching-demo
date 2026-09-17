package com.xb.pay.reconciliation.engine;

import com.xb.pay.reconciliation.downloader.BillDownloader;
import com.xb.pay.reconciliation.model.*;
import com.xb.pay.reconciliation.parser.BillParser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReconciliationEngine 对账引擎测试")
class ReconciliationEngineTest {

    private StubDownloader downloader;
    private StubParser parser;
    private ReconciliationEngine engine;

    @BeforeEach
    void setUp() {
        downloader = new StubDownloader();
        parser = new StubParser();
        engine = new ReconciliationEngine(downloader, parser, "ALIPAY", "2026-09-17");
    }

    // ========== 全部匹配 ==========

    @Test
    @DisplayName("全部匹配：渠道与本地完全一致，0差异")
    void reconcile_allMatched_noDiffs() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 17, 10, 0, 0);

        parser.records.add(channelRecord("CH001", "ORD001", new BigDecimal("100.00"), now, "TRADE_SUCCESS"));
        parser.records.add(channelRecord("CH002", "ORD002", new BigDecimal("200.00"), now, "TRADE_SUCCESS"));

        List<LocalOrderRecord> locals = List.of(
                localRecord("ORD001", new BigDecimal("100.00"), now),
                localRecord("ORD002", new BigDecimal("200.00"), now)
        );

        ReconReport report = engine.reconcile(locals);

        assertEquals(0, report.getDiffCount());
        assertTrue(report.isPassed());
        assertEquals(2, report.getMatchedCount());
        assertEquals(2, report.getTotalChannelCount());
        assertEquals(2, report.getTotalLocalCount());
    }

    // ========== 长款：渠道有、本地无 ==========

    @Test
    @DisplayName("长款检测：渠道有记录但本地不存在 → LONG_SHORT")
    void reconcile_longShort_detected() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 17, 10, 0, 0);

        parser.records.add(channelRecord("CH001", "ORD001", new BigDecimal("100.00"), now, "TRADE_SUCCESS"));

        ReconReport report = engine.reconcile(Collections.emptyList());

        assertEquals(1, report.getDiffCount());
        assertFalse(report.isPassed());
        ReconDiff diff = report.getDiffs().get(0);
        assertEquals(ReconDiff.DiffType.LONG_SHORT, diff.getDiffType());
        assertEquals("ORD001", diff.getOutTradeNo());
    }

    // ========== 短款：本地有、渠道无 ==========

    @Test
    @DisplayName("短款检测：本地有记录但渠道不存在 → SHORT_LONG")
    void reconcile_shortLong_detected() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 17, 10, 0, 0);

        List<LocalOrderRecord> locals = List.of(
                localRecord("ORD001", new BigDecimal("100.00"), now)
        );

        ReconReport report = engine.reconcile(locals);

        assertEquals(1, report.getDiffCount());
        ReconDiff diff = report.getDiffs().get(0);
        assertEquals(ReconDiff.DiffType.SHORT_LONG, diff.getDiffType());
        assertEquals("ORD001", diff.getOutTradeNo());
    }

    // ========== 金额不一致 ==========

    @Test
    @DisplayName("金额不一致：双方都有记录但金额不同 → AMOUNT_MISMATCH")
    void reconcile_amountMismatch_detected() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 17, 10, 0, 0);

        parser.records.add(channelRecord("CH001", "ORD001", new BigDecimal("100.00"), now, "TRADE_SUCCESS"));

        List<LocalOrderRecord> locals = List.of(
                localRecord("ORD001", new BigDecimal("99.00"), now)
        );

        ReconReport report = engine.reconcile(locals);

        assertEquals(1, report.getDiffCount());
        ReconDiff diff = report.getDiffs().get(0);
        assertEquals(ReconDiff.DiffType.AMOUNT_MISMATCH, diff.getDiffType());
        assertEquals(0, new BigDecimal("100.00").compareTo(diff.getChannelAmount()));
        assertEquals(0, new BigDecimal("99.00").compareTo(diff.getLocalAmount()));
    }

    // ========== 时间偏差 ==========

    @Test
    @DisplayName("时间偏差：金额一致但时间差超过5分钟 → TIME_MISMATCH")
    void reconcile_timeMismatch_detected() {
        LocalDateTime channelTime = LocalDateTime.of(2026, 9, 17, 10, 0, 0);
        LocalDateTime localTime = LocalDateTime.of(2026, 9, 17, 10, 10, 0);

        parser.records.add(channelRecord("CH001", "ORD001", new BigDecimal("100.00"), channelTime, "TRADE_SUCCESS"));

        List<LocalOrderRecord> locals = List.of(
                localRecord("ORD001", new BigDecimal("100.00"), localTime)
        );

        ReconReport report = engine.reconcile(locals);

        assertEquals(1, report.getDiffCount());
        ReconDiff diff = report.getDiffs().get(0);
        assertEquals(ReconDiff.DiffType.TIME_MISMATCH, diff.getDiffType());
    }

    @Test
    @DisplayName("时间偏差在阈值内（<=5分钟）：正常匹配")
    void reconcile_timeWithinThreshold_matched() {
        LocalDateTime channelTime = LocalDateTime.of(2026, 9, 17, 10, 0, 0);
        LocalDateTime localTime = LocalDateTime.of(2026, 9, 17, 10, 3, 0);

        parser.records.add(channelRecord("CH001", "ORD001", new BigDecimal("100.00"), channelTime, "TRADE_SUCCESS"));

        List<LocalOrderRecord> locals = List.of(
                localRecord("ORD001", new BigDecimal("100.00"), localTime)
        );

        ReconReport report = engine.reconcile(locals);

        assertEquals(0, report.getDiffCount());
        assertEquals(1, report.getMatchedCount());
    }

    // ========== 混合场景 ==========

    @Test
    @DisplayName("混合场景：同时存在匹配、长款、短款、金额不一致、时间偏差")
    void reconcile_mixedScenario() {
        LocalDateTime base = LocalDateTime.of(2026, 9, 17, 10, 0, 0);

        parser.records.add(channelRecord("CH001", "ORD_MATCH", new BigDecimal("100.00"), base, "TRADE_SUCCESS"));
        parser.records.add(channelRecord("CH002", "ORD_LONG", new BigDecimal("50.00"), base, "TRADE_SUCCESS"));
        parser.records.add(channelRecord("CH003", "ORD_AMOUNT", new BigDecimal("200.00"), base, "TRADE_SUCCESS"));
        parser.records.add(channelRecord("CH004", "ORD_TIME", new BigDecimal("80.00"), base, "TRADE_SUCCESS"));

        List<LocalOrderRecord> locals = new ArrayList<>();
        locals.add(localRecord("ORD_MATCH", new BigDecimal("100.00"), base));
        locals.add(localRecord("ORD_SHORT", new BigDecimal("30.00"), base));
        locals.add(localRecord("ORD_AMOUNT", new BigDecimal("199.00"), base));
        locals.add(localRecord("ORD_TIME", new BigDecimal("80.00"), base.plusMinutes(10)));

        ReconReport report = engine.reconcile(locals);

        assertEquals(4, report.getDiffCount());
        assertEquals(1, report.getMatchedCount());

        var diffTypes = report.getDiffs().stream()
                .map(ReconDiff::getDiffType)
                .sorted()
                .toList();
        assertTrue(diffTypes.contains(ReconDiff.DiffType.LONG_SHORT));
        assertTrue(diffTypes.contains(ReconDiff.DiffType.SHORT_LONG));
        assertTrue(diffTypes.contains(ReconDiff.DiffType.AMOUNT_MISMATCH));
        assertTrue(diffTypes.contains(ReconDiff.DiffType.TIME_MISMATCH));
    }

    // ========== 空数据 ==========

    @Test
    @DisplayName("空数据：双方都为空，0差异0匹配")
    void reconcile_emptyData() {
        ReconReport report = engine.reconcile(Collections.emptyList());

        assertEquals(0, report.getDiffCount());
        assertTrue(report.isPassed());
        assertEquals(0, report.getMatchedCount());
        assertEquals(0, report.getTotalChannelCount());
        assertEquals(0, report.getTotalLocalCount());
    }

    // ========== 报告统计 ==========

    @Test
    @DisplayName("报告统计：渠道/本地总金额正确汇总")
    void reconcile_amountTotals() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 17, 10, 0, 0);

        parser.records.add(channelRecord("CH001", "ORD001", new BigDecimal("100.00"), now, "TRADE_SUCCESS"));
        parser.records.add(channelRecord("CH002", "ORD002", new BigDecimal("200.00"), now, "TRADE_SUCCESS"));

        List<LocalOrderRecord> locals = List.of(
                localRecord("ORD001", new BigDecimal("100.00"), now),
                localRecord("ORD002", new BigDecimal("200.00"), now)
        );

        ReconReport report = engine.reconcile(locals);

        assertEquals(0, new BigDecimal("300.00").compareTo(report.getTotalChannelAmount()));
        assertEquals(0, new BigDecimal("300.00").compareTo(report.getTotalLocalAmount()));
        assertEquals("ALIPAY", report.getChannel());
        assertEquals("2026-09-17", report.getBillDate());
    }

    // ========== 辅助方法 ==========

    private ChannelBillRecord channelRecord(String transId, String outTradeNo,
                                            BigDecimal amount, LocalDateTime time, String status) {
        ChannelBillRecord r = new ChannelBillRecord();
        r.setTransId(transId);
        r.setOutTradeNo(outTradeNo);
        r.setAmount(amount);
        r.setTransTime(time);
        r.setTradeStatus(status);
        return r;
    }

    private LocalOrderRecord localRecord(String outTradeNo, BigDecimal amount, LocalDateTime time) {
        LocalOrderRecord r = new LocalOrderRecord();
        r.setOutTradeNo(outTradeNo);
        r.setPayAmount(amount);
        r.setPayTime(time);
        r.setTradeStatus("PAID");
        return r;
    }

    // ========== Stub 实现 ==========

    static class StubDownloader implements BillDownloader {
        @Override
        public InputStream download(String channel, String billDate) {
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    static class StubParser implements BillParser {
        final List<ChannelBillRecord> records = new ArrayList<>();

        @Override
        public List<ChannelBillRecord> parse(InputStream inputStream) {
            return records;
        }
    }
}

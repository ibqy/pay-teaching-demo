package com.xb.pay.reconciliation.parser;

import com.xb.pay.reconciliation.model.ChannelBillRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 微信支付 CSV 账单解析器
 * <p>
 * 微信支付对账单 CSV 字段顺序与支付宝不同，且金额单位是"分"（需要 ÷100 转成元）。
 * <pre>
 * 交易时间,商户订单号,微信支付流水号,交易金额(分),交易状态,商品名称,...
 * 2025-10-12 14:30:00,OUT20251012001,420000123420251012,4990,SUCCESS,测试商品
 * </pre>
 * ---- 对账知识点 ----
 * 微信对账单金额单位是"分"（int），支付宝是"元"（decimal），
 * 解析时统一转换为"元"，这样后续比对逻辑无需关心渠道差异。
 */
public class WechatCsvParser implements BillParser {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] HEADERS = {"trans_time", "out_trade_no", "transaction_id", "amount_fen", "status", "body"};

    /** 微信金额单位：分 → 元 */
    private static final BigDecimal FEN_TO_YUAN = new BigDecimal("100");

    @Override
    public List<ChannelBillRecord> parse(InputStream inputStream) {
        List<ChannelBillRecord> records = new ArrayList<>();

        try {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader(HEADERS)
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .setIgnoreEmptyLines(true)
                    .build();

            try (CSVParser parser = format.parse(new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                for (CSVRecord row : parser) {
                    // 跳过微信对账单末尾的汇总行（"总交易单数,总金额..." 以 "总" 开头）
                    String outTradeNo = row.get("out_trade_no");
                    if (outTradeNo.startsWith("总")) continue;

                    ChannelBillRecord r = new ChannelBillRecord();
                    r.setChannel("WECHAT");
                    r.setTransId(row.get("transaction_id"));
                    r.setOutTradeNo(outTradeNo);
                    // 微信金额单位是分，转成元
                    r.setAmount(new BigDecimal(row.get("amount_fen")).divide(FEN_TO_YUAN, 2, java.math.RoundingMode.HALF_UP));
                    r.setTransTime(LocalDateTime.parse(row.get("trans_time"), DT_FMT));
                    r.setTradeStatus(row.get("status"));
                    r.setBody(row.get("body"));
                    records.add(r);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("parse wechat bill csv error", e);
        }

        return records;
    }
}
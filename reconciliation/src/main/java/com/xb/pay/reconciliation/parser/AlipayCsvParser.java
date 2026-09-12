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
 * 支付宝 CSV 账单解析器
 * <p>
 * 支付宝对账单 CSV 格式（以 2025 年为例）：
 * <pre>
 * 支付宝交易号,商户订单号,交易金额,交易时间,交易状态,商品名称,...
 * 202510122200100001,OUT20251012001,49.90,2025-10-12 14:30:00,TRADE_SUCCESS,测试商品
 * </pre>
 * 第一行为表头，后面每行是一条记录。金额以"元"为单位，无需换算。
 */
public class AlipayCsvParser implements BillParser {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] HEADERS = {"trade_no", "out_trade_no", "amount", "trans_time", "status", "body"};

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
                    ChannelBillRecord r = new ChannelBillRecord();
                    r.setChannel("ALIPAY");
                    r.setTransId(row.get("trade_no"));
                    r.setOutTradeNo(row.get("out_trade_no"));
                    r.setAmount(new BigDecimal(row.get("amount")));
                    r.setTransTime(LocalDateTime.parse(row.get("trans_time"), DT_FMT));
                    r.setTradeStatus(row.get("status"));
                    r.setBody(row.get("body"));
                    records.add(r);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("parse alipay bill csv error", e);
        }

        return records;
    }
}
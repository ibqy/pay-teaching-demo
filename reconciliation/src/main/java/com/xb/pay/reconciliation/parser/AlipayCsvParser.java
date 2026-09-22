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
 * AlipayCsvParser - 支付宝 CSV 账单解析器
 *
 * 解析支付宝对账单 CSV（UTF-8 编码，逗号分隔），
 * 金额以"元"为单位，无需换算，直接映射为 ChannelBillRecord。
 *
 * @author ibqy
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
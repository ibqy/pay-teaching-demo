package com.xb.pay.reconciliation.parser;

import com.xb.pay.reconciliation.model.ChannelBillRecord;

import java.io.InputStream;
import java.util.List;

/**
 * 账单解析器接口 — 策略模式
 * <p>
 * 支付宝和微信的账单 CSV 文件格式不同（字段顺序、列名、分隔符都可能有差异），
 * 因此需要各自的实现类来解析。创建解析器实例时需要传入渠道标识。
 * <p>
 * ---- 对账知识点 ----
 * 支付宝 CSV：UTF-8 编码，字段顺序固定，以 , 分隔
 * 微信 CSV：UTF-8 with BOM 编码，字段顺序不同，以 ` 反引号分隔（视版本而定）
 */
public interface BillParser {

    /**
     * 将渠道账单的输入流解析为一组标准化的 ChannelBillRecord
     *
     * @param inputStream 账单文件流（由 Downloader 提供）
     * @return 解析后的标准化账单记录列表
     */
    List<ChannelBillRecord> parse(InputStream inputStream);
}
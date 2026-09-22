package com.xb.pay.reconciliation.parser;

import com.xb.pay.reconciliation.model.ChannelBillRecord;

import java.io.InputStream;
import java.util.List;

/**
 * BillParser - 账单解析器接口（策略模式）
 *
 * 支付宝和微信的账单格式不同（字段顺序、分隔符、金额单位），
 * 各渠道提供自己的解析实现，将原始文件流转为统一的 ChannelBillRecord。
 *
 * @author ibqy
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
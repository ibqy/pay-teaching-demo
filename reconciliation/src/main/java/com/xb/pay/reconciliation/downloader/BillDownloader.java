package com.xb.pay.reconciliation.downloader;

import java.io.InputStream;

/**
 * 渠道对账单下载器接口
 * <p>
 * 不同支付平台的对账单下载方式不同：
 * <ul>
 *   <li>支付宝：通过 alipay.data.dataservice.bill.downloadurl.query 获取下载链接</li>
 *   <li>微信：通过 电商平台→对账单下载 API（微信支付 API v3）直接下载</li>
 * </ul>
 * 该接口抽象了"获取对账单文件流"的行为，具体实现由渠道包（pay-alipay / pay-wechat）提供。
 */
public interface BillDownloader {

    /**
     * 下载指定日期的对账单
     *
     * @param billDate 账单日期，格式 yyyy-MM-dd
     * @return 账单文件输入流（调用方负责关闭）
     */
    InputStream download(String billDate);
}
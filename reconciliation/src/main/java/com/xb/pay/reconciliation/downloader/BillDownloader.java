package com.xb.pay.reconciliation.downloader;

import java.io.InputStream;

/**
 * BillDownloader - 渠道对账单下载器接口
 *
 * 抽象"获取对账单文件流"的行为，具体实现由渠道包提供。
 * 支付宝和微信的账单下载 API 不同，此处统一为 download(channel, date) 方法。
 *
 * @author ibqy
 */
public interface BillDownloader {

    /**
     * 下载指定渠道、指定日期的对账单
     *
     * @param channel  渠道标识（ALIPAY / WECHAT）
     * @param billDate 账单日期，格式 yyyy-MM-dd
     * @return 账单文件输入流（调用方负责关闭）
     */
    InputStream download(String channel, String billDate);
}
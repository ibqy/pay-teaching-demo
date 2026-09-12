package com.xb.pay.demo.reconciliation;

import com.xb.pay.reconciliation.downloader.BillDownloader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * 演示用账单下载器 — 从 classpath 读取模拟 CSV
 * <p>
 * 生产环境需对接支付宝/微信的真实账单下载 API：
 * <ul>
 *   <li>支付宝：alipay.data.dataservice.bill.downloadurl.query → 获取下载链接 → HTTP GET</li>
 *   <li>微信：电商平台对账单下载 API（GET /v3/bill/tradebill）</li>
 * </ul>
 */
@Component
public class DemoBillDownloader implements BillDownloader {

    @Override
    public InputStream download(String channel, String billDate) {
        String dateNum = billDate.replace("-", "");
        String path = "bill/" + channel.toLowerCase() + "_" + dateNum + ".csv";
        try {
            return new ClassPathResource(path).getInputStream();
        } catch (Exception e) {
            throw new RuntimeException("cannot load demo bill: " + path, e);
        }
    }
}
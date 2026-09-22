package com.xb.pay.demo.reconciliation;

import com.xb.pay.reconciliation.downloader.BillDownloader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * DemoBillDownloader - 演示用账单下载器，从 classpath 读取模拟 CSV
 *
 * 生产环境需对接真实账单下载 API：
 * 支付宝通过 downloadurl.query 获取链接，微信通过 GET /v3/bill/tradebill 下载。
 *
 * @author ibqy
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
package com.xb.pay.demo.reconciliation;

import com.xb.pay.reconciliation.model.ReconReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ReconScheduledTask - 定时对账任务，T+1 每日凌晨自动执行
 *
 * 生产环境每天凌晨 2:00 对昨日账单做全量核对，
 * 有差异时自动告警（邮件/企微），无差异自动归档。
 *
 * @author ibqy
 */
@Component
@EnableScheduling
public class ReconScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(ReconScheduledTask.class);

    private final ReconDemoService reconService;

    public ReconScheduledTask(ReconDemoService reconService) {
        this.reconService = reconService;
    }

    /**
     * 每天 02:00 执行对账（cron = 秒 分 时 日 月 周）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyRecon() {
        log.info("===== 开始定时对账 =====");

        ReconReport alipayReport = reconService.reconcileAlipay();
        log.info("支付宝对账结果: {}", alipayReport);

        ReconReport wechatReport = reconService.reconcileWechat();
        log.info("微信对账结果: {}", wechatReport);

        if (alipayReport.isPassed() && wechatReport.isPassed()) {
            log.info("对账全部通过，无差异");
        } else {
            log.warn("对账存在差异，请检查报告明细");
        }

        log.info("===== 定时对账完成 =====");
    }
}
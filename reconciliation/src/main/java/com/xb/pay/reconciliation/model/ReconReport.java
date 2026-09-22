package com.xb.pay.reconciliation.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * ReconReport - 对账报告（对账引擎的最终产出）
 *
 * 汇总统计（总笔数、匹配数、总金额）加差异明细，
 * 对账人员通过此报告判断是否需要人工介入处理。
 *
 * @author ibqy
 */
public class ReconReport {

    private String channel;                 // 渠道
    private String billDate;                // 账单日期（yyyy-MM-dd）
    private int totalChannelCount;          // 渠道总笔数
    private int totalLocalCount;            // 本地总笔数
    private int matchedCount;               // 一致笔数
    private BigDecimal totalChannelAmount;  // 渠道总金额
    private BigDecimal totalLocalAmount;    // 本地总金额
    private List<ReconDiff> diffs;          // 差异明细

    /** 汇总：差异总笔数 */
    public int getDiffCount() {
        return diffs == null ? 0 : diffs.size();
    }

    /** 汇总：是否通过（0 差异即为通过） */
    public boolean isPassed() {
        return getDiffCount() == 0;
    }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getBillDate() { return billDate; }
    public void setBillDate(String billDate) { this.billDate = billDate; }
    public int getTotalChannelCount() { return totalChannelCount; }
    public void setTotalChannelCount(int totalChannelCount) { this.totalChannelCount = totalChannelCount; }
    public int getTotalLocalCount() { return totalLocalCount; }
    public void setTotalLocalCount(int totalLocalCount) { this.totalLocalCount = totalLocalCount; }
    public int getMatchedCount() { return matchedCount; }
    public void setMatchedCount(int matchedCount) { this.matchedCount = matchedCount; }
    public BigDecimal getTotalChannelAmount() { return totalChannelAmount; }
    public void setTotalChannelAmount(BigDecimal totalChannelAmount) { this.totalChannelAmount = totalChannelAmount; }
    public BigDecimal getTotalLocalAmount() { return totalLocalAmount; }
    public void setTotalLocalAmount(BigDecimal totalLocalAmount) { this.totalLocalAmount = totalLocalAmount; }
    public List<ReconDiff> getDiffs() { return diffs; }
    public void setDiffs(List<ReconDiff> diffs) { this.diffs = diffs; }

    @Override
    public String toString() {
        return "ReconReport{" +
                "channel='" + channel + '\'' +
                ", billDate='" + billDate + '\'' +
                ", totalChannel=" + totalChannelCount +
                ", totalLocal=" + totalLocalCount +
                ", matched=" + matchedCount +
                ", diffs=" + getDiffCount() +
                ", pass=" + isPassed() +
                '}';
    }
}
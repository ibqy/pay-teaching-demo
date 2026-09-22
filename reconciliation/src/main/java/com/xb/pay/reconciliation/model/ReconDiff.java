package com.xb.pay.reconciliation.model;

import java.math.BigDecimal;

/**
 * ReconDiff - 对账差异记录
 *
 * 记录每一笔比对不一致的明细，差异类型包括：
 * LONG_SHORT（长款）、SHORT_LONG（短款）、AMOUNT_MISMATCH、TIME_MISMATCH。
 *
 * @author ibqy
 */
public class ReconDiff {

    public enum DiffType {
        LONG_SHORT,         // 渠道有、本地无 → 长款（平台多钱）
        SHORT_LONG,         // 本地有、渠道无 → 短款（平台少钱）
        AMOUNT_MISMATCH,    // 金额不一致
        TIME_MISMATCH       // 时间偏差大
    }

    private DiffType diffType;            // 差异类型
    private String outTradeNo;            // 商户订单号
    private BigDecimal channelAmount;     // 渠道金额
    private BigDecimal localAmount;       // 本地金额
    private String channelTransId;        // 渠道流水号
    private String localStatus;           // 本地订单状态
    private String channelStatus;         // 渠道交易状态
    private String remark;                // 备注说明

    public DiffType getDiffType() { return diffType; }
    public void setDiffType(DiffType diffType) { this.diffType = diffType; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public BigDecimal getChannelAmount() { return channelAmount; }
    public void setChannelAmount(BigDecimal channelAmount) { this.channelAmount = channelAmount; }
    public BigDecimal getLocalAmount() { return localAmount; }
    public void setLocalAmount(BigDecimal localAmount) { this.localAmount = localAmount; }
    public String getChannelTransId() { return channelTransId; }
    public void setChannelTransId(String channelTransId) { this.channelTransId = channelTransId; }
    public String getLocalStatus() { return localStatus; }
    public void setLocalStatus(String localStatus) { this.localStatus = localStatus; }
    public String getChannelStatus() { return channelStatus; }
    public void setChannelStatus(String channelStatus) { this.channelStatus = channelStatus; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    @Override
    public String toString() {
        return "ReconDiff{" +
                "diffType=" + diffType +
                ", outTradeNo='" + outTradeNo + '\'' +
                ", channelAmount=" + channelAmount +
                ", localAmount=" + localAmount +
                ", remark='" + remark + '\'' +
                '}';
    }
}
package com.xb.pay.common.exception;

/**
 * 支付异常
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>知识点</b>：支付异常需区分业务异常和系统异常。
 * 业务异常（余额不足、重复支付）应返回友好提示；
 * 系统异常（网络超时、验签失败）需记录日志并及时报警。</p>
 */
public class PayException extends RuntimeException {

    private final String code;

    public PayException(String code, String message) {
        super(message);
        this.code = code;
    }

    /** 是否可重试（网络超时等系统异常可重试） */
    public boolean isRetryable() {
        return "TIMEOUT".equals(code) || "NETWORK".equals(code);
    }

    public String code() { return code; }
}
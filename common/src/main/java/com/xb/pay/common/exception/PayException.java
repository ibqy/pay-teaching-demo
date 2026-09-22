package com.xb.pay.common.exception;

/**
 * PayException - 支付业务统一异常
 *
 * 携带错误码（code），便于区分业务异常和系统异常。
 * {@link #isRetryable()} 判断是否可重试（如网络超时），是支付容错的关键设计。
 *
 * @author ibqy
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
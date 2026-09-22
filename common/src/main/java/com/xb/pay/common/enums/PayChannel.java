package com.xb.pay.common.enums;

/**
 * PayChannel - 支付渠道枚举
 *
 * 定义系统支持的第三方支付渠道。
 * 新增渠道时只需在此添加枚举值并实现对应的 UnifiedPayService。
 *
 * @author ibqy
 */
public enum PayChannel {
    ALIPAY,   // 支付宝
    WECHAT    // 微信支付
}
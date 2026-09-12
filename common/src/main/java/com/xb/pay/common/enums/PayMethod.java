package com.xb.pay.common.enums;

/**
 * 支付方式（场景）
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>知识点</b>：不同场景对应不同的支付产品。
 * 支付宝：APP / 网站 / WAP 手机网页
 * 微信：JSAPI（公众号/小程序） / Native（扫码） / H5（手机网页） / APP</p>
 */
public enum PayMethod {
    /** 支付宝-电脑网站支付 ｜ 微信-Native扫码支付 */
    NATIVE,
    /** 微信-JSAPI（公众号/小程序内支付） */
    JSAPI,
    /** 支付宝-手机网页 ｜ 微信-H5唤醒支付 */
    H5,
    /** APP内支付 */
    APP
}
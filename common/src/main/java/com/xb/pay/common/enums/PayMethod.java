package com.xb.pay.common.enums;

/**
 * PayMethod - 支付方式枚举（支付场景）
 *
 * 不同场景对应不同的支付产品：支付宝支持 NATIVE/APP/H5，
 * 微信支持 NATIVE/JSAPI/H5/APP。NATIVE 在两端分别对应扫码和预下单。
 *
 * @author ibqy
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
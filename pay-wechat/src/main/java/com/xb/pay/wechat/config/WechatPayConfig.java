package com.xb.pay.wechat.config;

/**
 * 微信支付配置
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>知识点</b>：微信支付 API v3 使用证书认证（非 API 密钥模式），
 * 需要在商户平台（pay.weixin.qq.com）申请以下配置。
 * 开发阶段可使用"沙箱环境"模拟支付回调。</p>
 */
public class WechatPayConfig {

    /** 商户号（微信支付分配的商户号） */
    private String mchId;
    /** 商户 API v3 密钥（在商户平台设置） */
    private String apiV3Key;
    /** 商户私钥（PKCS8 格式，用于生成请求签名） */
    private String privateKey;
    /** 商户证书序列号（用于告知微信使用哪个证书验签） */
    private String merchantSerialNo;
    /** 公众号/小程序 AppId */
    private String appId;
    /** 通知地址 */
    private String notifyUrl;

    public String mchId() { return mchId; }
    public String apiV3Key() { return apiV3Key; }
    public String privateKey() { return privateKey; }
    public String merchantSerialNo() { return merchantSerialNo; }
    public String appId() { return appId; }
    public String notifyUrl() { return notifyUrl; }

    public void setMchId(String v) { mchId = v; }
    public void setApiV3Key(String v) { apiV3Key = v; }
    public void setPrivateKey(String v) { privateKey = v; }
    public void setMerchantSerialNo(String v) { merchantSerialNo = v; }
    public void setAppId(String v) { appId = v; }
    public void setNotifyUrl(String v) { notifyUrl = v; }
}
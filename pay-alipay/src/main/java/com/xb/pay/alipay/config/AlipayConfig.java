package com.xb.pay.alipay.config;

/**
 * 支付宝配置
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>知识点</b>：支付宝对接前需要准备以下配置，
 * 在蚂蚁开放平台（open.alipay.com）申请。
 * 开发阶段可使用"沙箱环境"模拟真实支付，无需真实商户资质。</p>
 */
public class AlipayConfig {

    /** 网关地址（生产：https://openapi.alipay.com/gateway.do ｜ 沙箱：https://openapi-sandbox.dl.alipaydev.com/gateway.do） */
    private String gatewayUrl;
    /** 应用 AppId（开放平台创建应用后获取） */
    private String appId;
    /** 应用私钥（PKCS8 格式，用于签名） */
    private String appPrivateKey;
    /** 支付宝公钥（用于验证异步通知签名） */
    private String alipayPublicKey;
    /** 签名类型（RSA2 推荐） */
    private String signType = "RSA2";
    /** 通知地址 */
    private String notifyUrl;
    /** 返回地址（同步通知，仅支付宝网页支付使用） */
    private String returnUrl;

    // getter / setter
    public String gatewayUrl() { return gatewayUrl; }
    public String appId() { return appId; }
    public String appPrivateKey() { return appPrivateKey; }
    public String alipayPublicKey() { return alipayPublicKey; }
    public String signType() { return signType; }
    public String notifyUrl() { return notifyUrl; }
    public String returnUrl() { return returnUrl; }

    public void setGatewayUrl(String v) { gatewayUrl = v; }
    public void setAppId(String v) { appId = v; }
    public void setAppPrivateKey(String v) { appPrivateKey = v; }
    public void setAlipayPublicKey(String v) { alipayPublicKey = v; }
    public void setSignType(String v) { signType = v; }
    public void setNotifyUrl(String v) { notifyUrl = v; }
    public void setReturnUrl(String v) { returnUrl = v; }
}
package com.xb.pay.common.model;

/**
 * PayResponse - 支付响应（下单后返回给客户端的支付参数）
 *
 * 不同支付方式返回内容不同：NATIVE 返回二维码 URL，
 * JSAPI/APP 返回唤起参数，H5 返回跳转链接，网页支付返回表单 HTML。
 *
 * @author ibqy
 */
public class PayResponse {

    /** 商户订单号 */
    private String outTradeNo;
    /** 三方流水号 */
    private String tradeNo;
    /** 二维码内容（NATIVE 支付使用） */
    private String codeUrl;
    /** 唤起支付参数（JSAPI/APP 使用） */
    private String payParams;
    /** 跳转链接（H5 支付使用） */
    private String redirectUrl;
    /** 表单 HTML（电脑网站支付使用） */
    private String formHtml;
    /** 附加数据（原样返回） */
    private String attach;

    public String outTradeNo() { return outTradeNo; }
    public String tradeNo() { return tradeNo; }
    public String codeUrl() { return codeUrl; }
    public String payParams() { return payParams; }
    public String redirectUrl() { return redirectUrl; }
    public String formHtml() { return formHtml; }
    public String attach() { return attach; }

    public void setOutTradeNo(String v) { outTradeNo = v; }
    public void setTradeNo(String v) { tradeNo = v; }
    public void setCodeUrl(String v) { codeUrl = v; }
    public void setPayParams(String v) { payParams = v; }
    public void setRedirectUrl(String v) { redirectUrl = v; }
    public void setFormHtml(String v) { formHtml = v; }
    public void setAttach(String v) { attach = v; }
}
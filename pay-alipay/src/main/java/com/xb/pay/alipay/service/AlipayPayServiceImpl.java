package com.xb.pay.alipay.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.xb.pay.alipay.config.AlipayConfig;
import com.xb.pay.common.enums.PayChannel;
import com.xb.pay.common.enums.PayMethod;
import com.xb.pay.common.enums.TradeStatus;
import com.xb.pay.common.exception.PayException;
import com.xb.pay.common.model.*;
import com.xb.pay.core.api.UnifiedPayService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 支付宝支付实现
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>知识点</b>：
 * <ul>
 *   <li>支付宝 SDK 核心类：AlipayClient（网关客户端）、AlipayRequest（请求）、AlipayResponse（响应）</li>
 *   <li>支付产品：电脑网站支付（trade.page.pay）、手机网站（trade.wap.pay）、扫码（trade.precreate）</li>
 *   <li>签名：商户请求用 appPrivateKey 签名，支付宝响应/通知用 alipayPublicKey 验签</li>
 * </ul></p>
 */
@Service
public class AlipayPayServiceImpl implements UnifiedPayService {

    private static final Logger log = LoggerFactory.getLogger(AlipayPayServiceImpl.class);

    private final AlipayConfig config;
    private AlipayClient client;

    public AlipayPayServiceImpl(AlipayConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        // 初始化支付宝客户端（线程安全）
        this.client = new DefaultAlipayClient(
            config.gatewayUrl(),
            config.appId(),
            config.appPrivateKey(),
            "json",
            "UTF-8",
            config.alipayPublicKey(),
            config.signType()
        );
    }

    @Override
    public PayChannel channel() {
        return PayChannel.ALIPAY;
    }

    @Override
    public PayResponse placeOrder(PayOrder order) {
        return switch (order.method()) {
            case NATIVE -> precreate(order);     // 扫码支付
            case H5 -> wapPay(order);            // 手机网页
            case APP -> appPay(order);           // APP 支付
            default -> pagePay(order);           // 电脑网站
        };
    }

    /** 扫码支付（支付宝推荐使用预下单模式，返回二维码链接） */
    private PayResponse precreate(PayOrder order) {
        try {
            var model = new AlipayTradePrecreateModel();
            model.setOutTradeNo(order.outTradeNo());
            model.setTotalAmount(order.amount().toPlainString());
            model.setSubject(order.description());
            model.setBody(order.description());
            model.setTimeExpire(order.expireAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            if (order.notifyUrl() != null) model.setNotifyUrl(order.notifyUrl());

            var request = new AlipayTradePrecreateRequest();
            request.setBizModel(model);

            AlipayTradePrecreateResponse resp = client.execute(request);
            if (!resp.isSuccess()) {
                throw new PayException(resp.getCode(), "支付宝下单失败：" + resp.getSubMsg());
            }

            var payResp = new PayResponse();
            payResp.setOutTradeNo(order.outTradeNo());
            payResp.setCodeUrl(resp.getQrCode()); // 二维码内容
            return payResp;
        } catch (AlipayApiException e) {
            throw new PayException("ALIPAY_ERROR", "支付宝下单异常：" + e.getMessage());
        }
    }

    /** 电脑网站支付（返回自动提交的表单 HTML） */
    private PayResponse pagePay(PayOrder order) {
        try {
            var model = new AlipayTradePagePayModel();
            model.setOutTradeNo(order.outTradeNo());
            model.setTotalAmount(order.amount().toPlainString());
            model.setSubject(order.description());
            model.setProductCode("FAST_INSTANT_TRADE_PAY");

            var request = new AlipayTradePagePayRequest();
            request.setBizModel(model);
            request.setNotifyUrl(config.notifyUrl());
            request.setReturnUrl(config.returnUrl());

            // 获取直接跳转链接（不走 execute，使用 pageExecute 获取表单/url）
            String form = client.pageExecute(request).getBody();

            var resp = new PayResponse();
            resp.setOutTradeNo(order.outTradeNo());
            resp.setFormHtml(form); // 前端直接渲染 form 后自动提交
            return resp;
        } catch (AlipayApiException e) {
            throw new PayException("ALIPAY_ERROR", "支付宝下单异常：" + e.getMessage());
        }
    }

    /** 手机网页支付 */
    private PayResponse wapPay(PayOrder order) {
        try {
            var model = new AlipayTradeWapPayModel();
            model.setOutTradeNo(order.outTradeNo());
            model.setTotalAmount(order.amount().toPlainString());
            model.setSubject(order.description());
            model.setProductCode("QUICK_WAP_WAY");

            var request = new AlipayTradeWapPayRequest();
            request.setBizModel(model);
            request.setNotifyUrl(config.notifyUrl());
            request.setReturnUrl(config.returnUrl());

            String form = client.pageExecute(request).getBody();

            var resp = new PayResponse();
            resp.setOutTradeNo(order.outTradeNo());
            resp.setFormHtml(form);
            return resp;
        } catch (AlipayApiException e) {
            throw new PayException("ALIPAY_ERROR", "支付宝下单异常：" + e.getMessage());
        }
    }

    /** APP 支付（返回 orderStr，前端调用支付宝 APP 支付） */
    private PayResponse appPay(PayOrder order) {
        try {
            var model = new AlipayTradeAppPayModel();
            model.setOutTradeNo(order.outTradeNo());
            model.setTotalAmount(order.amount().toPlainString());
            model.setSubject(order.description());
            model.setProductCode("QUICK_MSECURITY_PAY");

            var request = new AlipayTradeAppPayRequest();
            request.setBizModel(model);
            request.setNotifyUrl(config.notifyUrl());

            String orderStr = client.sdkExecute(request).getBody();

            var resp = new PayResponse();
            resp.setOutTradeNo(order.outTradeNo());
            resp.setPayParams(orderStr); // 前端用此参数调起支付宝 APP
            return resp;
        } catch (AlipayApiException e) {
            throw new PayException("ALIPAY_ERROR", "支付宝下单异常：" + e.getMessage());
        }
    }

    @Override
    public PayOrder queryOrder(String outTradeNo) {
        try {
            var model = new AlipayTradeQueryModel();
            model.setOutTradeNo(outTradeNo);

            var request = new AlipayTradeQueryRequest();
            request.setBizModel(model);

            AlipayTradeQueryResponse resp = client.execute(request);
            if (!resp.isSuccess()) {
                throw new PayException(resp.getCode(), "支付宝查询失败：" + resp.getSubMsg());
            }

            var order = new PayOrder();
            order.setOutTradeNo(outTradeNo);
            order.setTradeNo(resp.getTradeNo());
            order.setAmount(new BigDecimal(resp.getTotalAmount()));
            order.setStatus(parseAlipayStatus(resp.getTradeStatus()));
            return order;
        } catch (AlipayApiException e) {
            throw new PayException("ALIPAY_ERROR", "支付宝查询异常：" + e.getMessage());
        }
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        try {
            var model = new AlipayTradeRefundModel();
            model.setOutTradeNo(request.outTradeNo());
            model.setRefundAmount(request.refundAmount().toPlainString());
            model.setRefundReason(request.reason());

            var req = new AlipayTradeRefundRequest();
            req.setBizModel(model);

            AlipayTradeRefundResponse resp = client.execute(req);
            if (!resp.isSuccess()) {
                throw new PayException(resp.getCode(), "支付宝退款失败：" + resp.getSubMsg());
            }

            var refundResp = new RefundResponse();
            refundResp.setOutTradeNo(request.outTradeNo());
            refundResp.setRefundNo(resp.getTradeNo());
            refundResp.setStatus("Y".equals(resp.getFundChange()) ? TradeStatus.REFUNDED : TradeStatus.REFUNDING);
            return refundResp;
        } catch (AlipayApiException e) {
            throw new PayException("ALIPAY_ERROR", "支付宝退款异常：" + e.getMessage());
        }
    }

    @Override
    public boolean closeOrder(String outTradeNo) {
        try {
            var model = new AlipayTradeCloseModel();
            model.setOutTradeNo(outTradeNo);

            var request = new AlipayTradeCloseRequest();
            request.setBizModel(model);

            AlipayTradeCloseResponse resp = client.execute(request);
            return resp.isSuccess();
        } catch (AlipayApiException e) {
            log.warn("支付宝关单失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public NotifyResult parseNotify(String rawBody, String signature, String channel) {
        // 支付宝通知：参数在 rawBody 中，签名在 sign 参数中
        // 通知格式：application/x-www-form-urlencoded → Map
        // 此处简化，实际需调用 AlipaySignature.rsaCheckV1 验签
        // 下面演示实际项目中验签和解析的完整流程

        // 实际验签代码（注释掉，仅展示）：
        // boolean signVerified = AlipaySignature.rsaCheckV1(paramsMap, config.alipayPublicKey(), "UTF-8", "RSA2");
        // if (!signVerified) throw new PayException("SIGN_FAIL", "支付宝通知验签失败");

        // 解析通知参数...
        var result = new NotifyResult();
        result.setChannel("alipay");
        // result.setOutTradeNo(paramsMap.get("out_trade_no"));
        // result.setTradeNo(paramsMap.get("trade_no"));
        // result.setAmount(new BigDecimal(paramsMap.get("total_amount")));
        // result.setBuyerId(paramsMap.get("buyer_id"));
        return result;
    }

    private TradeStatus parseAlipayStatus(String tradeStatus) {
        return switch (tradeStatus) {
            case "TRADE_SUCCESS" -> TradeStatus.SUCCESS;
            case "TRADE_FINISHED" -> TradeStatus.SUCCESS;
            case "WAIT_BUYER_PAY" -> TradeStatus.WAITING;
            case "TRADE_CLOSED" -> TradeStatus.CLOSED;
            default -> TradeStatus.FAILED;
        };
    }
}
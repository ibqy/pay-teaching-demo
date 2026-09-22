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
 * AlipayPayServiceImpl - 支付宝支付实现（UnifiedPayService 的支付宝策略）
 *
 * 演示对接支付宝 SDK 的核心流程：签名下单、查询订单、退款、关单、解析异步通知。
 * 支持扫码（precreate）、网站（pagePay）、手机网页（wapPay）、APP 四种支付方式。
 *
 * @author ibqy
 */
@Service
public class AlipayPayServiceImpl implements UnifiedPayService {

    @Override
    public PayChannel channel() {
        return PayChannel.ALIPAY;
    }

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
        // 支付宝通知格式：application/x-www-form-urlencoded → Map
        // 参数包含 sign（签名）和 sign_type（签名类型）两个字段
        Map<String, String> params = parseFormBody(rawBody);

        // 第一步：验签 — 使用支付宝公钥验证通知参数未被篡改
        boolean signVerified;
        try {
            signVerified = AlipaySignature.rsaCheckV1(
                params,
                config.alipayPublicKey(),
                "UTF-8",
                config.signType()
            );
        } catch (AlipayApiException e) {
            throw new PayException("SIGN_FAIL", "支付宝通知验签异常：" + e.getMessage());
        }
        if (!signVerified) {
            throw new PayException("SIGN_FAIL", "支付宝通知验签失败，签名不匹配");
        }

        // 第二步：解析通知参数
        var result = new NotifyResult();
        result.setChannel("alipay");
        result.setOutTradeNo(params.get("out_trade_no"));
        result.setTradeNo(params.get("trade_no"));
        result.setAmount(new BigDecimal(params.get("total_amount")));
        result.setBuyerId(params.get("buyer_id"));

        // 支付时间（格式：yyyy-MM-dd HH:mm:ss）
        if (params.get("gmt_payment") != null) {
            result.setPaidAt(LocalDateTime.parse(params.get("gmt_payment"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        result.setRawParams(rawBody);
        return result;
    }

    /** 将 URL-encoded 表单字符串解析为 Map */
    private Map<String, String> parseFormBody(String rawBody) {
        var params = new java.util.HashMap<String, String>();
        if (rawBody == null || rawBody.isBlank()) {
            return params;
        }
        for (String pair : rawBody.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String key = java.net.URLDecoder.decode(kv[0], java.nio.charset.StandardCharsets.UTF_8);
                String value = java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
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

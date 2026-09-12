package com.xb.pay.wechat.service;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.*;
import com.wechat.pay.java.service.payments.jsapi.JsapiPayService;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.payments.h5.H5PayService;
import com.wechat.pay.java.service.payments.h5.model.*;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.xb.pay.common.enums.PayChannel;
import com.xb.pay.common.enums.PayMethod;
import com.xb.pay.common.enums.TradeStatus;
import com.xb.pay.common.exception.PayException;
import com.xb.pay.common.model.*;
import com.xb.pay.core.api.UnifiedPayService;
import com.xb.pay.wechat.config.WechatPayConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 微信支付实现（API v3）
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>知识点</b>：
 * <ul>
 *   <li>微信支付 API v3 与 v2 的区别：v3 用 RESTful API + JSON 格式，v2 用 XML</li>
 *   <li>证书体系：商户私钥签名请求，微信公钥验签回调</li>
 *   <li>支付产品：Native（扫码）、JSAPI（公众号/小程序）、H5（手机网页）、App</li>
 *   <li>金额单位：微信金额以"分"为单位（整数），与支付宝"元"（小数）不同，
 *       对接时需注意转换：元 × 100 = 分</li>
 *   <li>回调通知：POST 到 notifyUrl，通过 Wechatpay-Signature 和 Wechatpay-Timestamp 验签，
 *       回调 body 是加密的 JSON，需要用 apiV3Key 解密</li>
 * </ul></p>
 */
@Service
public class WechatPayServiceImpl implements UnifiedPayService {

    private static final Logger log = LoggerFactory.getLogger(WechatPayServiceImpl.class);

    private final WechatPayConfig config;
    private Config sdkConfig;
    private NativePayService nativePayService;
    private JsapiPayService jsapiPayService;
    private H5PayService h5PayService;
    private RefundService refundService;

    public WechatPayServiceImpl(WechatPayConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        this.sdkConfig = new RSAAutoCertificateConfig.Builder()
            .merchantId(config.mchId())
            .privateKey(config.privateKey())
            .merchantSerialNumber(config.merchantSerialNo())
            .apiV3Key(config.apiV3Key())
            .build();

        this.nativePayService = new NativePayService.Builder().config(sdkConfig).build();
        this.jsapiPayService = new JsapiPayService.Builder().config(sdkConfig).build();
        this.h5PayService = new H5PayService.Builder().config(sdkConfig).build();
        this.refundService = new RefundService.Builder().config(sdkConfig).build();
    }

    @Override
    public PayChannel channel() {
        return PayChannel.WECHAT;
    }

    @Override
    public PayResponse placeOrder(PayOrder order) {
        return switch (order.method()) {
            case NATIVE -> nativePay(order);
            case JSAPI -> jsapiPay(order);
            case H5 -> h5Pay(order);
            default -> throw new IllegalArgumentException("微信支付不支持该方式：" + order.method());
        };
    }

    /** Native 扫码支付：返回二维码 URL */
    private PayResponse nativePay(PayOrder order) {
        var req = new PrepayRequest();
        req.setAppid(config.appId());
        req.setMchid(config.mchId());
        req.setDescription(order.description());
        req.setOutTradeNo(order.outTradeNo());
        req.setNotifyUrl(config.notifyUrl());

        var amount = new com.wechat.pay.java.service.payments.nativepay.model.Amount();
        amount.setTotal(yuanToFen(order.amount())); // 元 → 分
        amount.setCurrency("CNY");
        req.setAmount(amount);

        PrepayResponse resp = nativePayService.prepay(req);

        var payResp = new PayResponse();
        payResp.setOutTradeNo(order.outTradeNo());
        payResp.setCodeUrl(resp.getCodeUrl()); // 二维码链接，前端转为二维码图片
        return payResp;
    }

    /** JSAPI 支付：返回 prepay_id，前端调起微信支付 */
    private PayResponse jsapiPay(PayOrder order) {
        var req = new com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest();
        req.setAppid(config.appId());
        req.setMchid(config.mchId());
        req.setDescription(order.description());
        req.setOutTradeNo(order.outTradeNo());
        req.setNotifyUrl(config.notifyUrl());

        var amount = new com.wechat.pay.java.service.payments.jsapi.model.Amount();
        amount.setTotal(yuanToFen(order.amount()));
        amount.setCurrency("CNY");
        req.setAmount(amount);

        // JSAPI 必须传入 openId
        // var payer = new Payer(); payer.setOpenId("用户 openId");
        // req.setPayer(payer);

        com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse resp = jsapiPayService.prepay(req);

        var payResp = new PayResponse();
        payResp.setOutTradeNo(order.outTradeNo());
        payResp.setPayParams(resp.getPrepayId()); // 前端用此 ID 调起支付
        return payResp;
    }

    /** H5 支付：返回跳转链接 */
    private PayResponse h5Pay(PayOrder order) {
        var req = new com.wechat.pay.java.service.payments.h5.model.PrepayRequest();
        req.setAppid(config.appId());
        req.setMchid(config.mchId());
        req.setDescription(order.description());
        req.setOutTradeNo(order.outTradeNo());
        req.setNotifyUrl(config.notifyUrl());

        var amount = new com.wechat.pay.java.service.payments.h5.model.Amount();
        amount.setTotal(yuanToFen(order.amount()));
        amount.setCurrency("CNY");
        req.setAmount(amount);

        // H5 场景信息（选填）
        var sceneInfo = new H5SceneInfo();
        sceneInfo.setType("Wap");
        req.setSceneInfo(sceneInfo);

        com.wechat.pay.java.service.payments.h5.model.PrepayResponse resp = h5PayService.prepay(req);

        var payResp = new PayResponse();
        payResp.setOutTradeNo(order.outTradeNo());
        payResp.setRedirectUrl(resp.getH5Url()); // H5 跳转链接
        return payResp;
    }

    @Override
    public PayOrder queryOrder(String outTradeNo) {
        var req = new com.wechat.pay.java.service.payments.model.QueryOrderByOutTradeNoRequest();
        req.setMchid(config.mchId());
        req.setOutTradeNo(outTradeNo);

        Transaction tx = nativePayService.queryOrderByOutTradeNo(req);

        var order = new PayOrder();
        order.setOutTradeNo(outTradeNo);
        order.setTradeNo(tx.getTransactionId());
        order.setAmount(fenToYuan(tx.getAmount().getTotal()));
        order.setStatus(parseWechatStatus(tx.getTradeState()));
        return order;
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        var req = new CreateRequest();
        req.setOutTradeNo(request.outTradeNo());
        req.setReason(request.reason());
        req.setNotifyUrl(config.notifyUrl());

        int fen = yuanToFen(request.refundAmount());
        var amount = new AmountReq();
        amount.setRefund(fen);
        amount.setTotal(fen); // 演示：全额退款
        amount.setCurrency("CNY");
        req.setAmount(amount);

        Refund refund = refundService.create(req);

        var resp = new RefundResponse();
        resp.setOutTradeNo(request.outTradeNo());
        resp.setRefundNo(refund.getRefundId());
        resp.setStatus("SUCCESS".equals(refund.getStatus()) ? TradeStatus.REFUNDED : TradeStatus.REFUNDING);
        return resp;
    }

    @Override
    public boolean closeOrder(String outTradeNo) {
        try {
            var req = new com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest();
            req.setMchid(config.mchId());
            req.setOutTradeNo(outTradeNo);
            nativePayService.closeOrder(req);
            return true;
        } catch (Exception e) {
            log.warn("微信关单失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public NotifyResult parseNotify(String rawBody, String signature, String channel) {
        // 微信通知处理流程：验签 → 解密 → 解析
        // 通知 Header：Wechatpay-Signature, Wechatpay-Timestamp, Wechatpay-Nonce, Wechatpay-Serial
        // 通知 Body：加密的 JSON（AES-GCM 加密）
        // 解密后得到 Transaction 对象，包含 outTradeNo、transactionId 等

        var parser = new NotificationParser((NotificationConfig) sdkConfig);
        var param = new RequestParam();
        // param.setSerial(header("Wechatpay-Serial"));
        // param.setSignature(header("Wechatpay-Signature"));
        // param.setTimestamp(header("Wechatpay-Timestamp"));
        // param.setNonce(header("Wechatpay-Nonce"));
        // param.setBody(rawBody);
        // Transaction tx = parser.parse(param, Transaction.class);

        var result = new NotifyResult();
        result.setChannel("wechat");
        return result;
    }

    /** 元 → 分（微信金额单位转换） */
    private int yuanToFen(BigDecimal yuan) {
        return yuan.multiply(BigDecimal.valueOf(100)).intValue();
    }

    /** 分 → 元 */
    private BigDecimal fenToYuan(Integer fen) {
        return BigDecimal.valueOf(fen).divide(BigDecimal.valueOf(100));
    }

    /** 微信交易状态 → 统一状态 */
    private TradeStatus parseWechatStatus(Transaction.TradeStateEnum state) {
        return switch (state) {
            case SUCCESS -> TradeStatus.SUCCESS;
            case NOTPAY  -> TradeStatus.WAITING;
            case CLOSED  -> TradeStatus.CLOSED;
            case REFUND  -> TradeStatus.REFUNDING;
            default      -> TradeStatus.FAILED;
        };
    }
}
# 支付宝异步通知处理指南

> 作者：xb | 日期：2026-09-12

## 为什么异步通知是支付对接中最关键的环节？

用户支付成功后，支付宝会向商户服务器 `POST` 通知（notifyUrl）。如果处理不当：

- **漏处理** → 用户付了钱但订单仍是"待支付"
- **重复处理** → 多次发货、多次扣库存
- **未验签** → 伪造通知导致订单被恶意标记为"已支付"

## 异步通知流程

```
用户支付成功
     │
     ▼
支付宝 POST notifyUrl ──→ 商户服务器接收
     │                        │
     │                        ├─ 1. 验签（防伪造）
     │                        ├─ 2. 判断交易状态
     │                        ├─ 3. 幂等处理（防重复）
     │                        ├─ 4. 更新订单状态
     │                        └─ 5. 返回 "success"
     │                                    │
     ◄────────────────────────────────────┘
```

## 验签实现

支付宝异步通知的参数格式为 `application/x-www-form-urlencoded`，包含签名参数 `sign` 和签名类型 `sign_type`。

验签使用 `AlipaySignature.rsaCheckV1()` 方法：

```java
@Override
public NotifyResult parseNotify(String rawBody, String signature, String channel) {
    // 1. 解析 URL-encoded 表单参数
    Map<String, String> params = parseFormBody(rawBody);

    // 2. 调用支付宝 SDK 验签
    boolean signVerified;
    try {
        signVerified = AlipaySignature.rsaCheckV1(
            params,
            config.alipayPublicKey(),  // 支付宝公钥
            "UTF-8",
            config.signType()          // RSA2
        );
    } catch (AlipayApiException e) {
        throw new PayException("SIGN_FAIL", "验签异常：" + e.getMessage());
    }

    if (!signVerified) {
        throw new PayException("SIGN_FAIL", "签名验证失败");
    }

    // 3. 验签通过后解析业务参数
    var result = new NotifyResult();
    result.setChannel("alipay");
    result.setOutTradeNo(params.get("out_trade_no"));
    result.setTradeNo(params.get("trade_no"));
    result.setAmount(new BigDecimal(params.get("total_amount")));
    result.setBuyerId(params.get("buyer_id"));

    // 支付时间
    if (params.get("gmt_payment") != null) {
        result.setPaidAt(LocalDateTime.parse(params.get("gmt_payment"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    return result;
}
```

### 表单参数解析辅助方法

```java
/** 将 URL-encoded 表单字符串解析为 Map */
private Map<String, String> parseFormBody(String rawBody) {
    var params = new HashMap<String, String>();
    if (rawBody == null || rawBody.isBlank()) {
        return params;
    }
    for (String pair : rawBody.split("&")) {
        String[] kv = pair.split("=", 2);
        if (kv.length == 2) {
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            params.put(key, value);
        }
    }
    return params;
}
```

## 支付宝通知常见参数

| 参数名 | 说明 | 示例 |
|--------|------|------|
| `notify_time` | 通知发送时间 | `2024-12-01 10:30:00` |
| `notify_type` | 通知类型 | `trade_status_sync` |
| `out_trade_no` | 商户订单号 | `20260912143012000001` |
| `trade_no` | 支付宝交易号 | `2026091222001481234567` |
| `trade_status` | 交易状态 | `TRADE_SUCCESS` |
| `total_amount` | 交易金额（元） | `0.01` |
| `buyer_id` | 买家支付宝用户号 | `2088000000000000` |
| `gmt_payment` | 交易付款时间 | `2024-12-01 10:29:58` |
| `sign` | 签名 | — |
| `sign_type` | 签名类型 | `RSA2` |

## 幂等处理

支付宝保证通知一定会送达，但不保证只送达一次。因此业务代码必须做幂等：

```java
@Idempotent(key = "#outTradeNo", ttl = 600)
@Transactional
public void handleNotify(String outTradeNo, NotifyResult result, String channel) {
    orderRepo.findByOutTradeNo(outTradeNo).ifPresentOrElse(entity -> {
        // 已处理过：如果已是终态，直接返回（幂等）
        if (TradeStatus.SUCCESS.name().equals(entity.getStatus())) {
            log.info("通知重复，已处理: {}", outTradeNo);
            return;
        }
        // 更新为支付成功
        entity.setStatus(TradeStatus.SUCCESS.name());
        entity.setTradeNo(result.tradeNo());
        entity.setPaidAt(LocalDateTime.now());
        orderRepo.save(entity);
    }, () -> {
        // 通知先于下单（极端情况）：创建订单记录
        var e = new PayOrderEntity();
        e.setOutTradeNo(outTradeNo);
        e.setStatus(TradeStatus.SUCCESS.name());
        orderRepo.save(e);
    });
}
```

## 状态更新逻辑

支付宝通知中的 `trade_status` 及其对应的处理策略：

| trade_status | 含义 | 处理策略 |
|-------------|------|---------|
| `WAIT_BUYER_PAY` | 等待买家付款 | 一般不通知，可忽略 |
| `TRADE_SUCCESS` | 支付成功 | **更新订单为已支付，触发发货等后续流程** |
| `TRADE_FINISHED` | 交易完成（已过退款期） | 标记终态，不做额外处理 |
| `TRADE_CLOSED` | 交易关闭 | 更新订单为已关闭 |

## 返回格式

处理完通知后，**必须**返回纯文本 `"success"`（不含 HTML/JSON），否则支付宝会认为通知失败并重发（最多 5 次，间隔 4m/10m/10m/1h/2h/6h/15h）。

```java
@PostMapping("/notify/alipay")
public String alipayNotify(@RequestBody String body) {
    var svc = payFactory.get(PayChannel.ALIPAY);
    NotifyResult result = svc.parseNotify(body, null, "alipay");
    String outTradeNo = result.outTradeNo() != null
        ? result.outTradeNo() : "NOTIFY_DEMO";
    notifyService.handleNotify(outTradeNo, result, "ALIPAY");
    return "success";  // ← 必须是这个字符串
}
```

## 生产建议

1. **验签是第一道防线**：任何不签名或签名不匹配的通知直接丢弃
2. **幂等 + 事务**：确保同一笔订单不会被重复处理
3. **异步处理**：通知接收和业务处理解耦，先返回 `success` 再异步处理
4. **日志记录**：留存原始通知参数（`rawParams`），便于排查问题
5. **主动查询兜底**：定时任务主动查询未支付订单，防止通知丢失
6. **沙箱测试**：上线前在 [支付宝沙箱环境](https://openhome.alipay.com/platform/appDaily.htm) 充分验证

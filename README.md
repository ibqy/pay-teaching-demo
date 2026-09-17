# 💳 支付对接教学项目

> 作者：xb ｜ 日期：2026-09-12
>
> 支付宝 + 微信支付官方 SDK 对接实战项目，覆盖支付对接全流程：
> **下单 → 异步通知 → 查询 → 退款 → 关单  → 对账**，一步不落。

## 架构

```
        ┌───────────────── pay-demo ─────────────────┐
        │  PayDemoController   +   index.html (UI)    │
        │  IdempotentAspect（幂等切面）               │
        │  PayEventPublisher / Consumer / DLQ         │
        │  PayOrderRepository（JPA 持久化）           │
        └──────┬─────────────────────┬────────────────┘
               │                     │
        ┌──────┴────────────┐ ┌──────┴──────────────┐
        │   pay-core        │ │ reconciliation      │
        │ 统一支付接口+工厂  │ │ 对账引擎+账单解析    │
        │ RoutingEngine路由  │ │                     │
        └──────┬────────────┘ └─────────────────────┘
               │
        ┌──────┴──────┐          ┌───────┴───────┐
        │ pay-alipay  │          │  pay-wechat   │
        │ alipay-sdk  │          │ wechatpay-java│
        └─────────────┘          └───────────────┘
               │                         │
               └─────── common ──────────┘
               支付模型 / 枚举(状态机) / 幂等注解 / 事件 / 异常 / 工具
```

## 6 模块

| 模块 | 内容 | 官方 SDK |
|------|------|----------|
| [`common`](common) | 支付模型、枚举、异常、单号生成 | — |
| [`pay-core`](pay-core) | UnifiedPayService 统一接口 + PayStrategyFactory 策略工厂 | — |
| [`pay-alipay`](pay-alipay) | Alipay 对接：下单/查询/退款/关单/通知验签 | alipay-sdk-java |
| [`pay-wechat`](pay-wechat) | WeChat 对接：下单/查询/退款/关单/通知验签 | wechatpay-java |
| [`reconciliation`](reconciliation) | 对账引擎：账单下载 → CSV解析 → 逐笔比对 → 差异报告 | commons-csv |
| [`pay-demo`](pay-demo) | Spring Boot 演示（Controller + 前端 UI + 配置） | — |

## 覆盖的知识点

| 类别 | 知识点 | 对应代码 |
|------|--------|----------|
| **支付流程** | 统一下单 → 异步通知 → 查询 → 退款 → 关单 | `PayDemoController` |
| **设计模式** | 策略模式（多支付渠道） | `UnifiedPayService` + `PayStrategyFactory` |
| **签名验签** | RSA2 签名 / Wechatpay-Signature 验签 | `AlipayPayServiceImpl` / `WechatPayServiceImpl` |
| **幂等性** | 异步通知必须去重处理 | `NotifyResult` |
| **幂等切面** | `@Idempotent` 注解 + AOP 拦截（SpEL key + TTL） | `IdempotentAspect` |
| **金额单位** | 支付宝"元" / 微信"分" 转换 | `yuanToFen` / `fenToYuan` |
| **沙箱环境** | 支付宝沙箱地址 vs 生产地址 | `application.yml` |
| **状态机** | 交易状态流转 + `canTransitionTo` 守卫 | `TradeStatus` |
| **异常处理** | 业务异常 vs 系统异常 | `PayException` |
| **订单号** | 唯一订单号生成策略 | `OrderNoGenerator` |
| **对账** | 渠道账单下载 → CSV解析 → 逐笔比对 → 差异报告 | `ReconciliationEngine` |
| **差异分析** | 长款/短款/金额不符/时间偏差 | `ReconDiff` |
| **单位转换** | 微信对账单金额单位"分"转"元" | `WechatCsvParser` |
| **事件驱动** | Spring Event 异步事件 + 重试 + 死信队列 | `PayEventPublisher` / `Consumer` / `DeadLetterQueue` |
| **DB 持久化** | JPA 实体 + Repository + 生命周期回调 | `PayOrderEntity` / `PayOrderRepository` |
| **路由引擎** | 费率 + 权重 + 支付方式兼容性 → 最优渠道选择 | `RoutingEngine` / `ChannelMeta` |

## 快速启动

```bash
# 前置：JDK 21+、Maven 3.9+
cd pay-teaching-demo

# 启动（无需构建）
mvn -pl pay-demo spring-boot-run

# 打开浏览器访问
open http://localhost:8080
```

启动后可以在线演示：支付宝扫码下单 → 查询 → 退款全流程。

## 对接前需要准备的配置

### 支付宝
1. 登录 [open.alipay.com](https://open.alipay.com) → 创建网页/移动应用
2. 获取 AppId、生成应用私钥、设置支付宝公钥
3. 配置接口加签方式（RSA2）
4. 设置授权回调地址（notifyUrl、returnUrl）

### 微信支付
1. 登录 [pay.weixin.qq.com](https://pay.weixin.qq.com) → 开通商户号
2. 获取商户号 mchId、设置 APIv3 密钥
3. 生成商户证书（私钥 + 证书序列号）
4. 设置支付回调通知地址

### 开发环境
两个渠道都提供沙箱环境：
- **支付宝沙箱**：`gatewayUrl` 用 sandbox 地址，配套沙箱版 APP 扫码
- **微信沙箱**：使用测试商户号 + 测试款

配置填写在 `pay-demo/src/main/resources/application.yml`。

## 目录结构

```
pay-teaching-demo/
├── pom.xml                          # 聚合父工程
├── common/                          # 公共模块
│   └── src/main/java/com/xb/pay/common/
│       ├── annotation/  @Idempotent（幂等注解：SpEL key + TTL）
│       ├── enums/       PayChannel, PayMethod, TradeStatus（状态机）
│       ├── event/       PayEvent（异步事件 + 重试计数）
│       ├── model/       PayOrder, PayResponse, NotifyResult, RefundRequest/Response
│       ├── exception/   PayException
│       └── util/        OrderNoGenerator
├── pay-core/                        # 支付核心（统一接口 + 策略工厂 + 路由）
│   └── src/main/java/com/xb/pay/core/
│       ├── api/         UnifiedPayService
│       ├── strategy/    PayStrategyFactory
│       └── routing/     RoutingEngine, ChannelMeta
├── pay-alipay/                      # 支付宝对接
│   └── src/main/java/com/xb/pay/alipay/
│       ├── config/      AlipayConfig
│       └── service/     AlipayPayServiceImpl
├── pay-wechat/                      # 微信支付对接
│   └── src/main/java/com/xb/pay/wechat/
│       ├── config/      WechatPayConfig
│       └── service/     WechatPayServiceImpl
├── reconciliation/                  # 对账模块
│   └── src/main/java/com/xb/pay/reconciliation/
│       ├── model/       ChannelBillRecord, ReconDiff, ReconReport
│       ├── parser/      BillParser(接口), AlipayCsvParser, WechatCsvParser
│       ├── downloader/  BillDownloader(接口)
│       └── engine/      ReconciliationEngine
├── pay-demo/                        # 演示应用
│   └── src/main/
│       ├── java/com/xb/pay/demo/
│       │   ├── PayDemoApplication
│       │   ├── config/   PayConfig
│       │   ├── controller/PayDemoController
│       │   ├── aspect/   IdempotentAspect（AOP 幂等拦截）
│       │   ├── event/    PayEventPublisher, PayEventConsumer, DeadLetterQueue
│       │   └── db/       PayOrderEntity, PayOrderRepository（JPA 持久化）
│       └── resources/
│           ├── application.yml
│           └── templates/index.html
├── docs/                            # 教学文档
│   └── 01-pay-knowledge.md
└── README.md
```

## 实现边界

### 已实现（可直接运行）

| 功能 | 说明 |
|------|------|
| 统一下单/查询/退款/关单 | 支付宝 + 微信双渠道完整流程 |
| 异步通知接收与验签 | RSA2 / Wechatpay-Signature 验签 |
| 策略模式路由 | `UnifiedPayService` + `PayStrategyFactory` 多渠道切换 |
| 对账引擎 | FULL OUTER JOIN 比对：长款/短款/金额不符/时间偏差 |
| 订单号生成器 | 时间戳 + 原子序列，保证唯一性 |
| 路由引擎 | 基于费率/权重/支付方式兼容性的渠道自动选择 |
| **状态机守卫** | `TradeStatus.canTransitionTo()` 防止非法状态流转 |
| **幂等切面** | `@Idempotent` 注解 + AOP（SpEL key + TTL 过期） |
| **事件驱动 + DLQ** | Spring Event 异步处理 + 3 次重试 + 死信队列 |
| **DB 持久化** | JPA Entity + Repository + `@PrePersist`/`@PreUpdate` 时间戳 |

### 教学简化（生产需增强）

| 简化点 | 生产做法 |
|--------|----------|
| `OrderNoGenerator` 单机 AtomicLong | 雪花算法（Snowflake）或数据库序列，保证分布式唯一 |
| 对账数据全量加载到内存 | 生产按分页/流式处理，避免大账单 OOM |
| `BillDownloader` 接口无真实实现 | 生产需对接支付宝/微信对账单下载 API |
| 路由引擎固定权重 | 生产可接入动态权重、熔断、灰度 |

### 未实现（需真实商户号）

| 功能 | 说明 |
|------|------|
| 支付宝/微信 SDK 真实调用 | 需配置真实 AppId / 商户号 / 密钥 |
| 对账单真实下载 | 需开通商户对账权限 |
| 差异自动处理（补单/退款） | 生产需根据差异类型自动或人工介入处理 |

## 测试覆盖

| 模块 | 测试类 | 用例数 | 覆盖场景 |
|------|--------|--------|----------|
| `common` | `OrderNoGeneratorTest` | 5 | 格式校验、唯一性、序列递增 |
| `common` | `TradeStatusTest` | 12 | 状态机流转（WAITING→SUCCESS/FAILED/CLOSED、SUCCESS→REFUNDING、终态不可变） |
| `reconciliation` | `ReconciliationEngineTest` | 9 | 全匹配、长款、短款、金额不符、时间偏差、混合场景、空数据、金额汇总 |
| **合计** | | **26** | |

```bash
mvn test -pl common,reconciliation -am
```

## License

仅用于教学交流，作者：xb

<p align="center">
  <a href="https://github.com/ibqy">🏠 回到 ibqy 主页</a> · <a href="https://ibqy.github.io">🌐 作品集</a>
</p>
---
layout: home

hero:
  name: 支付对接教学
  text: 支付宝 + 微信支付 SDK 全流程
  tagline: 统一支付接口 + 策略工厂 + 渠道路由：下单、异步通知验签、退款、对账引擎，附幂等与死信队列生产实践
  actions:
    - theme: brand
      text: 开始学习 →
      link: /01-pay-knowledge
    - theme: alt
      text: GitHub 源码
      link: https://github.com/ibqy/pay-teaching-demo

features:
  - icon: 💳
    title: 统一支付接口
    details: 一个接口对接多支付渠道，业务层不感知渠道差异
  - icon: 🏭
    title: 策略工厂与渠道路由
    details: 按渠道分发到对应策略实现，新增渠道零侵入
  - icon: 🔔
    title: 异步通知验签
    details: 支付宝异步回调的验签、幂等处理与应答规范
  - icon: 🧾
    title: 对账引擎
    details: 渠道账单与本地流水比对，差异单定位与处理
  - icon: 🔁
    title: 幂等与重试
    details: "@Idempotent 注解防重复扣款，重试有边界不放大故障"
  - icon: ☠️
    title: 死信队列
    details: 通知处理失败的兜底路径，避免丢单
  - icon: ▶️
    title: 一键运行
    details: mvn -pl pay-demo spring-boot:run 即起服务，沙箱环境可复现
---

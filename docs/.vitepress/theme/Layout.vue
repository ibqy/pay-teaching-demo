<script setup>
import DefaultTheme from 'vitepress/theme'

const { Layout } = DefaultTheme
</script>

<template>
  <Layout>
    <template #home-features-before>
      <section class="sig">
        <div class="sig-card">
          <span class="sig-label">LIFECYCLE · ONE PAYMENT</span>
          <h3 class="sig-title">一笔支付的安全旅程</h3>
          <div class="pay-flow">
            <span class="sg-node">下单</span>
            <span class="sg-arr">→</span>
            <span class="sg-node">统一支付接口</span>
            <span class="sg-arr">→</span>
            <span class="sg-node">策略工厂 · 渠道路由</span>
            <span class="sg-arr">→</span>
            <div class="pay-ch">
              <span>支付宝</span>
              <span>微信支付</span>
            </div>
            <span class="sg-arr">→</span>
            <span class="sg-fill">异步通知 · 验签</span>
            <span class="sg-arr">→</span>
            <span class="sg-fill">幂等入库</span>
            <span class="sg-arr">→</span>
            <span class="sg-line">业务发货</span>
          </div>
          <div class="pay-guards">
            <div class="pay-guard"><b>死信队列</b> —— 通知处理失败的兜底路径，重试有边界，不丢单、不放大故障</div>
            <div class="pay-guard"><b>对账引擎</b> —— 渠道账单 ⇄ 本地流水逐笔比对，差异单可定位、可处理</div>
          </div>
          <p class="sig-note">支付对接的难点从来不是发起扣款，而是回调可信、重复无害、金额对得上——这条链路上的每个环，项目里都有对应的代码实现。</p>
        </div>
      </section>
    </template>
    <template #home-features-after>
      <div class="sig-more"><a href="https://ibqy.github.io/" target="_blank" rel="noopener">← ibqy.github.io · 探索全部项目</a></div>
    </template>
  </Layout>
</template>

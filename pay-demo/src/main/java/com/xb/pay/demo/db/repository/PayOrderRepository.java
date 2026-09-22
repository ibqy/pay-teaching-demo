package com.xb.pay.demo.db.repository;

import com.xb.pay.demo.db.entity.PayOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * PayOrderRepository - 支付订单数据访问层
 *
 * 继承 JpaRepository，提供按商户订单号查询、判重、统计等查询方法。
 * outTradeNo 的唯一约束在 Entity 层定义，此处利用 Spring Data 命名规则自动生成 SQL。
 *
 * @author ibqy
 */
public interface PayOrderRepository extends JpaRepository<PayOrderEntity, Long> {

    Optional<PayOrderEntity> findByOutTradeNo(String outTradeNo);

    boolean existsByOutTradeNo(String outTradeNo);

    long countByStatus(String status);
}

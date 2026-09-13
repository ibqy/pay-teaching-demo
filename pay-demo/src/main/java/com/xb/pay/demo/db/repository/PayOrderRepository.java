package com.xb.pay.demo.db.repository;

import com.xb.pay.demo.db.entity.PayOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayOrderRepository extends JpaRepository<PayOrderEntity, Long> {

    Optional<PayOrderEntity> findByOutTradeNo(String outTradeNo);

    boolean existsByOutTradeNo(String outTradeNo);

    long countByStatus(String status);
}

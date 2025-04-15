package com.example.otel.repository;

import com.example.otel.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findTopByOrderByIdDesc(); // 최근 주문 하나만 가져오기

}

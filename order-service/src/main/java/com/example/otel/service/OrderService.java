package com.example.otel.service;

import com.example.otel.entity.Inventory;
import com.example.otel.entity.OrderEntity;
import com.example.otel.repository.InventoryRepository;
import com.example.otel.repository.OrderRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    public OrderService(OrderRepository orderRepository,  InventoryRepository inventoryRepository) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @WithSpan("check-stock")
    public void checkStock(String productName) {
        Span span = Span.current();
        try {
            Inventory inventory = inventoryRepository.findByProductName(productName)
                    .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다"));

            if (inventory.getStock() <= 0) {
                throw new RuntimeException("재고 없음");
            }

            span.setStatus(StatusCode.OK);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, "재고 확인 실패");
            span.recordException(e);
            throw e;
        }
    }


    @WithSpan("call-payment-api")
    public void callPaymentAPI(int delay) {
        Span span = Span.current();
        try {
            String url = "http://localhost:8081/pay?delay=" + delay;
            long start = System.currentTimeMillis();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            long duration = System.currentTimeMillis() - start;

            if (response.getStatusCode().is5xxServerError() || duration > 500) {
                throw new RuntimeException("결제 실패 - 상태: " + response.getStatusCode() + ", 시간: " + duration + "ms");
            }

            span.setStatus(StatusCode.OK);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, "결제 실패 : 시간 초과");
            span.recordException(e);
            throw new RuntimeException("Payment service failed", e);
        }
    }

    @WithSpan("save-order")
    public void saveOrder() {
        Span span = Span.current();
        try {
            Thread.sleep(30);
            orderRepository.save(new OrderEntity(1)); // 기본 재고 1 저장
            span.setStatus(StatusCode.OK);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, "저장 실패");
            span.recordException(e);
            throw new RuntimeException("DB save failed", e);
        }
    }
}

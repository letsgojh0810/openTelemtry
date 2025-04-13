package com.example.otel.controller;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class OrderController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/order")
    @WithSpan
    public ResponseEntity<String> order() {
        try {
            checkStock();
            callPaymentAPI();
            saveOrder();
            return ResponseEntity.ok("Order complete!");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("주문 실패: " + e.getMessage());
        }
    }


    @WithSpan("check-stock")
    public void checkStock() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @WithSpan("call-payment-api")
    public void callPaymentAPI() {
        Span span = Span.current();
        try {
            String url = "http://localhost:8081/pay"; // 고정 호출
            long start = System.currentTimeMillis();

            restTemplate.getForObject(url, String.class);

            long duration = System.currentTimeMillis() - start;

            if (duration > 500) {
                throw new RuntimeException("응답 지연: " + duration + "ms");
            }

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, "결제 실패 : 시간 초과");
            span.recordException(e);
            throw new RuntimeException("Payment service failed", e);
        }
    }


    @WithSpan("save-order")
    public void saveOrder() {
        try {
            Thread.sleep(30);
            Span.current().setStatus(StatusCode.OK); // ✅ 여기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

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
public class otelController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/order")
    @WithSpan
    public ResponseEntity<String> order(@RequestParam(defaultValue = "success") String caseType) {
        try {
            checkStock();

            switch (caseType) {
                case "fail" -> callPaymentAPI(false, false);       // 빠른 실패
                case "delayed" -> callPaymentAPI(true, false);     // 지연 후 실패
                default -> callPaymentAPI(true, true);             // 성공
            }

            saveOrder();
            return ResponseEntity.ok("🛒 Order complete!");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ 주문 실패: " + e.getMessage());
        }
    }

    @WithSpan("check-stock")
    public void checkStock() {
        try {
            Thread.sleep(50); // 재고 확인 시간 시뮬레이션
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @WithSpan("call-payment-api")
    public void callPaymentAPI(boolean delay, boolean success) {
        Span span = Span.current();
        try {
            String url = "https://httpstat.us/";
            if (success) {
                url += "200";
            } else {
                url += delay ? "500?sleep=300" : "500"; // 지연 + 에러 or 즉시 에러
            }

            restTemplate.getForObject(url, String.class);

            if (!success) {
                throw new RuntimeException("에러 발생!");
            }
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, "결제 실패");
            span.recordException(e);
            throw new RuntimeException("Payment service failed", e);
        }
    }

    @WithSpan("save-order")
    public void saveOrder() {
        try {
            Thread.sleep(30); // DB 저장 시간 시뮬레이션
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

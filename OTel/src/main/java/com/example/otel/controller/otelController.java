package com.example.otel.controller;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

@RestController
public class otelController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/hello")
    @WithSpan
    public String hello() {
        return "👋 Hello from OpenTelemetry!";
    }

    @GetMapping("/order")
    @WithSpan
    public String order() {
        checkStock();
        callPaymentAPI();
        saveOrder();
        return "🛒 Order complete!";
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
    public void callPaymentAPI() {
        Span span = Span.current();
        try {
            restTemplate.getForObject("https://httpstat.us/500", String.class);
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

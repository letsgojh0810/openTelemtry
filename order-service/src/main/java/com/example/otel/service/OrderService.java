package com.example.otel.service;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Service
public class OrderService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    @WithSpan("check-stock")
    public void checkStock() {
        Span span = Span.current();
        try {
            Thread.sleep(50);
            // 랜덤 재고 실패 시뮬레이션
            if (random.nextInt(10) < 2) { // 20% 확률로 재고 부족
                throw new RuntimeException("재고 부족");
            }
            span.setStatus(StatusCode.OK);
        } catch (InterruptedException e) {
            span.setStatus(StatusCode.ERROR, "재고 확인 중단");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, "재고 실패");
            span.recordException(e);
            throw e;
        }
    }

    @WithSpan("call-payment-api")
    public void callPaymentAPI(int delay) {
        Span span = Span.current();
        try {
            String url = "http://localhost:8081/pay?delay=" + delay;  // delay 전달
            long start = System.currentTimeMillis();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            long duration = System.currentTimeMillis() - start;

            if (response.getStatusCode().is5xxServerError() || duration > 500) {
                throw new RuntimeException("결제 실패 - 상태: " + response.getStatusCode() + ", 시간: " + duration + "ms");
            }

            span.setStatus(StatusCode.OK);

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, "결제 실패");
            span.recordException(e);
            throw new RuntimeException("Payment service failed", e);
        }
    }


    @WithSpan("save-order")
    public void saveOrder() {
        Span span = Span.current();
        try {
            Thread.sleep(30);
            // 10% 확률로 DB 저장 실패
            if (random.nextInt(10) < 1) {
                throw new RuntimeException("DB 저장 실패");
            }
            span.setStatus(StatusCode.OK);
        } catch (InterruptedException e) {
            span.setStatus(StatusCode.ERROR, "저장 중단됨");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, "주문 저장 실패");
            span.recordException(e);
            throw e;
        }
    }
}

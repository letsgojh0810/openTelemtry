package com.example.otel.controller;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PayController {

    @GetMapping("/pay")
    @WithSpan
    public ResponseEntity<String> pay(@RequestParam(defaultValue = "100") long delay) {
        Span span = Span.current();
        long start = System.currentTimeMillis();

        try {
            // 요청 지연 시뮬레이션
            Thread.sleep(delay);

            long duration = System.currentTimeMillis() - start;
            if (duration > 500) {
                throw new RuntimeException("응답 지연: " + duration + "ms");
            }

            return ResponseEntity.ok("💳 결제 성공!");

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, "결제 실패");
            span.recordException(e);
            return ResponseEntity.status(500).body("❌ 결제 실패: " + e.getMessage());
        }
    }
}

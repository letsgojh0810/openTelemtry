package com.example.otel.controller;

import com.example.otel.service.OrderService;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 루트 스팬
    @GetMapping("/order")
    @WithSpan
    public ResponseEntity<String> order(@RequestParam String productName, @RequestParam(defaultValue = "0") int delay) {
        Span span = Span.current();

        try {
            orderService.checkStock(productName);
            orderService.callPaymentAPI(delay);
            orderService.saveOrder();

            span.setStatus(StatusCode.OK);
            return ResponseEntity.ok("Order complete!");

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, "주문 실패");
            span.recordException(e);
            return ResponseEntity.status(500).body("주문 실패: " + e.getMessage());
        }
    }


}

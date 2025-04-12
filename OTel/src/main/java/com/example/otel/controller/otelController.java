package com.example.otel.controller;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.With;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class otelController {

    @GetMapping("/hello")
    @WithSpan
    public String hello() {
        return "👋 Hello from OpenTelemetry!";
    }
}
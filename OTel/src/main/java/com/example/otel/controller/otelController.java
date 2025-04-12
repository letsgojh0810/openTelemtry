package com.example.otel.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class otelController {

    @GetMapping("/hello")
    public String hello() {
        return "👋 Hello from OpenTelemetry!";
    }
}
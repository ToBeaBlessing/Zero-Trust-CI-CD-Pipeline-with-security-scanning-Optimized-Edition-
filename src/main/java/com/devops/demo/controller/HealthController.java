package com.devops.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;
import java.time.Instant;

@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "secure-api");
        response.put("status", "running");
        response.put("timestamp", Instant.now().toString());
        response.put("message", "Welcome to Secure API - Built with Zero-Trust CI/CD");
        return response;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "secure-api");
        return health;
    }

    @GetMapping("/api/info")
    public Map<String, Object> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "Secure API Demo");
        info.put("version", "1.0.0");
        info.put("java_version", System.getProperty("java.version"));
        info.put("security_scanning", "Trivy");
        info.put("pipeline", "Jenkins");
        return info;
    }
}
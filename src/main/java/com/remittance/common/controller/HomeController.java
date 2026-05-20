package com.remittance.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {

        return Map.of(
                "service", "quote-remittance-backend",
                "status", "running",
                "version", "1.0.0"
        );
    }
}

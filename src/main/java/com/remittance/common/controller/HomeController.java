package com.remittance.common.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    private final String serviceName;
    private final String appVersion;

    public HomeController(
            @Value("${spring.application.name}") String serviceName,
            @Value("${app.version:0.0.1-SNAPSHOT}") String appVersion
    ) {
        this.serviceName = serviceName;
        this.appVersion = appVersion;
    }

    @GetMapping("/")
    public Map<String, Object> home() {

        return Map.of(
                "service", this.serviceName,
                "status", "running",
                "version", this.appVersion
        );
    }
}

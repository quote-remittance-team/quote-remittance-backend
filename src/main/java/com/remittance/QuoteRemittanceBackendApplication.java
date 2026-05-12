package com.remittance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class QuoteRemittanceBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuoteRemittanceBackendApplication.class, args);
    }
}

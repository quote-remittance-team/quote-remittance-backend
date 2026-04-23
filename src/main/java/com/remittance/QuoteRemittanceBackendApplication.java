package com.remittance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication

// To activate the timestamps
@EnableJpaAuditing
public class QuoteRemittanceBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuoteRemittanceBackendApplication.class, args);
    }
}

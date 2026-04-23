package com.remittance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// Tells spring it's a settings file and to read it on startup
@Configuration
// timestamp activator
@EnableJpaAuditing

public class JpaAuditingConfig {
}

package com.remittance.common.util;

import java.util.Set;

public final class CurrencyValidator {

    private CurrencyValidator() {}

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
            "USD",
            "NGN",
            "EUR",
            "GBP"
    );

    public static String normalize(String currency) {

        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }

        String normalized = currency.trim().toUpperCase();

        if (!SUPPORTED_CURRENCIES.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported currency");
        }

        return normalized;
    }
}

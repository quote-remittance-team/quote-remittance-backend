package com.remittance.quote.service.impl;

import com.remittance.enums.Currency;
import com.remittance.enums.QuoteStatus;
import com.remittance.quote.dto.CreateQuoteRequest;
import com.remittance.quote.dto.QuoteResponse;
import com.remittance.quote.entity.Quote;
import com.remittance.quote.repository.QuoteRepository;
import com.remittance.quote.service.QuoteService;
import com.remittance.user.entity.User;
import com.remittance.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public QuoteResponse generateQuote(CreateQuoteRequest request) {

        validateCurrency(request.getFromCurrency());
        validateCurrency(request.getToCurrency());

        if (request.getFromCurrency()
                .equalsIgnoreCase(request.getToCurrency())) {

            throw new IllegalArgumentException(
                    "Source and destination currencies cannot be the same"
            );
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BigDecimal exchangeRate = fetchExchangeRate(
                request.getFromCurrency(),
                request.getToCurrency()
        );

        BigDecimal fee = calculateFee(request.getSendAmount());

        BigDecimal receiveAmount = request.getSendAmount()
                .multiply(exchangeRate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalPayable = request.getSendAmount().add(fee);

        Quote quote = Quote.builder()
                .quoteReference(generateQuoteReference())
                .user(user)
                .sendAmount(request.getSendAmount())
                .fromCurrency(Currency.valueOf(request.getFromCurrency().toUpperCase()))
                .toCurrency(Currency.valueOf(request.getToCurrency().toUpperCase()))
                .exchangeRate(exchangeRate)
                .fee(fee)
                .receiveAmount(receiveAmount)
                .totalPayable(totalPayable)
                .status(QuoteStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        Quote savedQuote = quoteRepository.save(quote);

        return QuoteResponse.builder()
                .quoteId(savedQuote.getId())
                .quoteReference(savedQuote.getQuoteReference())
                .sendAmount(savedQuote.getSendAmount())
                .fromCurrency(savedQuote.getFromCurrency().name())
                .toCurrency(savedQuote.getToCurrency().name())
                .exchangeRate(savedQuote.getExchangeRate())
                .fee(savedQuote.getFee())
                .receiveAmount(savedQuote.getReceiveAmount())
                .totalPayable(savedQuote.getTotalPayable())
                .expiresAt(savedQuote.getExpiresAt())
                .build();

    }

    private void validateCurrency(String currency) {

        try {
            Currency.valueOf(currency.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported currency");
        }
    }

    /**
     * Temporary mock exchange rate logic.
     * External FX provider integration comes later.
     */
    private BigDecimal fetchExchangeRate(
            String fromCurrency,
            String toCurrency
    ) {
        if (fromCurrency.equalsIgnoreCase("USD")
                && toCurrency.equalsIgnoreCase("NGN")) {

            return BigDecimal.valueOf(1600);
        }

        if (fromCurrency.equalsIgnoreCase("GBP")
                && toCurrency.equalsIgnoreCase("NGN")) {

            return BigDecimal.valueOf(2100);
        }

        return BigDecimal.ONE;
    }

    private BigDecimal calculateFee(BigDecimal amount) {

        return amount.multiply(BigDecimal.valueOf(0.015))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String generateQuoteReference() {

        return "QTE-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}

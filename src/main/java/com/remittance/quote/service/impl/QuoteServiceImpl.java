package com.remittance.quote.service.impl;

import com.remittance.common.util.CurrencyValidator;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final UserRepository userRepository;

    @Value("${quote.expiry-minutes}")
    private long quoteExpiryMinutes;

    @Value("${quote.fee-percentage}")
    private BigDecimal feePercentage;

    @Override
    @Transactional
    public QuoteResponse generateQuote(CreateQuoteRequest request) {

        String fromCurrency = CurrencyValidator.normalize(request.getFromCurrency());

        String toCurrency = CurrencyValidator.normalize(request.getToCurrency());

        if (fromCurrency.equalsIgnoreCase(toCurrency)) {

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
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .exchangeRate(exchangeRate)
                .fee(fee)
                .receiveAmount(receiveAmount)
                .totalPayable(totalPayable)
                .status(QuoteStatus.ACTIVE)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(quoteExpiryMinutes)))
                .build();

        Quote savedQuote = quoteRepository.save(quote);

        return QuoteResponse.builder()
                .quoteId(savedQuote.getId())
                .quoteReference(savedQuote.getQuoteReference())
                .sendAmount(savedQuote.getSendAmount())
                .fromCurrency(savedQuote.getFromCurrency())
                .toCurrency(savedQuote.getToCurrency())
                .exchangeRate(savedQuote.getExchangeRate())
                .fee(savedQuote.getFee())
                .receiveAmount(savedQuote.getReceiveAmount())
                .totalPayable(savedQuote.getTotalPayable())
                .expiresAt(savedQuote.getExpiresAt())
                .build();

    }

    @Override
    public  QuoteResponse getQuoteById(UUID id) {

        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Quote not found"
                        ));

        return QuoteResponse.builder()
                .quoteId(quote.getId())
                .quoteReference(quote.getQuoteReference())
                .sendAmount(quote.getSendAmount())
                .fromCurrency(quote.getFromCurrency())
                .toCurrency(quote.getToCurrency())
                .exchangeRate(quote.getExchangeRate())
                .fee(quote.getFee())
                .receiveAmount(quote.getReceiveAmount())
                .totalPayable(quote.getTotalPayable())
                .expiresAt(quote.getExpiresAt())
                .build();
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

        return amount.multiply(feePercentage)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String generateQuoteReference() {

        return "QTE-" + UUID.randomUUID()
                .toString()
                .toUpperCase();
    }
}

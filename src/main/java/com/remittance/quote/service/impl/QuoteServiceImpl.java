package com.remittance.quote.service.impl;

import com.remittance.common.util.CurrencyValidator;
import com.remittance.enums.QuoteStatus;
import com.remittance.integration.exchange.ExchangeRateClient;
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
    private final ExchangeRateClient exchangeRateClient;

    @Value("${quote.expiry-minutes}")
    private long quoteExpiryMinutes;

    @Value("${quote.fee-percentage}")
    private BigDecimal feePercentage;

    @Override
    @Transactional
    public QuoteResponse generateQuote(CreateQuoteRequest request, String userEmail) {

        String fromCurrency = CurrencyValidator.normalize(request.getFromCurrency());

        String toCurrency = CurrencyValidator.normalize(request.getToCurrency());

        if (fromCurrency.equalsIgnoreCase(toCurrency)) {

            throw new IllegalArgumentException(
                    "Source and destination currencies cannot be the same"
            );
        }

        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BigDecimal exchangeRate =
                exchangeRateClient.getExchangeRate(
                        fromCurrency,
                        toCurrency
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

        return mapToResponse(savedQuote);

    }

    @Override
    public  QuoteResponse getQuoteById(UUID id, String userEmail) {

        Quote quote = quoteRepository.findByIdAndUserEmailIgnoreCase(id, userEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Quote not found"
                        ));

        return mapToResponse(quote);
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

    private QuoteResponse mapToResponse(Quote quote) {

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
}

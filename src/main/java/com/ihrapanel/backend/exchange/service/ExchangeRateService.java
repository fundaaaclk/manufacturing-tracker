package com.ihrapanel.backend.exchange.service;

import com.ihrapanel.backend.common.exception.ConflictException;
import com.ihrapanel.backend.common.exception.ResourceNotFoundException;
import com.ihrapanel.backend.common.money.Currency;
import com.ihrapanel.backend.exchange.entity.ExchangeRate;
import com.ihrapanel.backend.exchange.entity.ExchangeRateSource;
import com.ihrapanel.backend.exchange.repository.ExchangeRateRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateService(
            ExchangeRateRepository exchangeRateRepository
    ) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public ExchangeRate getLatestRateAt(
            Currency baseCurrency,
            Currency quoteCurrency,
            ExchangeRateSource source,
            Instant effectiveAt
    ) {

        return exchangeRateRepository
                .findFirstByBaseCurrencyAndQuoteCurrencyAndSourceAndEffectiveAtLessThanEqualOrderByEffectiveAtDesc(
                        baseCurrency,
                        quoteCurrency,
                        source,
                        effectiveAt
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Belirtilen tarih için döviz kuru bulunamadı."
                        )
                );
    }

//kur kaydetme kısmı
public ExchangeRate createRate(
        Currency baseCurrency,
        Currency quoteCurrency,
        BigDecimal rate,
        Instant effectiveAt,
        ExchangeRateSource source
) {


    if (baseCurrency == null) {
        throw new IllegalArgumentException(
                "Kaynak para birimi boş olamaz."
        );
    }

    if (quoteCurrency == null) {
        throw new IllegalArgumentException(
                "Hedef para birimi boş olamaz."
        );
    }

    if (baseCurrency == quoteCurrency) {
        throw new IllegalArgumentException(
                "Kaynak ve hedef para birimi aynı olamaz."
        );
    }

    if (rate == null) {
        throw new IllegalArgumentException(
                "Döviz kuru boş olamaz."
        );
    }

    if (rate.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException(
                "Döviz kuru sıfırdan büyük olmalıdır."
        );
    }

    if (effectiveAt == null) {
        throw new IllegalArgumentException(
                "Kur zamanı boş olamaz."
        );
    }

    if (source == null) {
        throw new IllegalArgumentException(
                "Kur kaynağı boş olamaz."
        );
    }
    if (exchangeRateRepository
        .existsByBaseCurrencyAndQuoteCurrencyAndEffectiveAtAndSource(
                baseCurrency,
                quoteCurrency,
                effectiveAt,
                source
        )) {

    throw new ConflictException(
            "Bu zaman ve kaynak için döviz kuru zaten mevcut."
    );
}

    ExchangeRate exchangeRate = new ExchangeRate();
    exchangeRate.setBaseCurrency(baseCurrency);
    exchangeRate.setQuoteCurrency(quoteCurrency);
    exchangeRate.setRate(rate);
    exchangeRate.setEffectiveAt(effectiveAt);
    exchangeRate.setSource(source);

    return exchangeRateRepository.save(exchangeRate);
}




}
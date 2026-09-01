package com.ihrapanel.backend.exchange.repository;

import com.ihrapanel.backend.common.money.Currency;
import com.ihrapanel.backend.exchange.entity.ExchangeRate;
import com.ihrapanel.backend.exchange.entity.ExchangeRateSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ExchangeRateRepository
        extends JpaRepository<ExchangeRate, UUID> {

    Optional<ExchangeRate>
    findFirstByBaseCurrencyAndQuoteCurrencyAndSourceAndEffectiveAtLessThanEqualOrderByEffectiveAtDesc(
            Currency baseCurrency,
            Currency quoteCurrency,
            ExchangeRateSource source,
            Instant effectiveAt
    );
       boolean existsByBaseCurrencyAndQuoteCurrencyAndEffectiveAtAndSource(
            Currency baseCurrency,
            Currency quoteCurrency,
            Instant effectiveAt,
            ExchangeRateSource source
    );
}
package com.ihrapanel.backend.common.money;

import com.ihrapanel.backend.exchange.entity.ExchangeRateSource;

import java.math.BigDecimal;
import java.time.Instant;

public record MoneySnapshot(
        BigDecimal originalAmount,
        Currency originalCurrency,
        BigDecimal fxRate,
        BigDecimal tryEquivalent,
        ExchangeRateSource fxRateSource,
        Instant fxRateEffectiveAt
) {
}
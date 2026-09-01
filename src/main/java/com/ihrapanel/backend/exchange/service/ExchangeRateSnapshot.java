package com.ihrapanel.backend.exchange.service;

import com.ihrapanel.backend.exchange.entity.ExchangeRateSource;

import java.math.BigDecimal;
import java.time.Instant;

public record ExchangeRateSnapshot(
        BigDecimal rate,
        ExchangeRateSource source,
        Instant effectiveAt
) {
}

//setter olmaz çünkü snapsot mantıgından  tam olarak istediğimiz sey böyle bişi 
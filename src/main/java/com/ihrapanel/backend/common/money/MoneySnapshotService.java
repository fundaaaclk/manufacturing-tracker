package com.ihrapanel.backend.common.money;

import com.ihrapanel.backend.exchange.entity.ExchangeRateSource;
import com.ihrapanel.backend.exchange.service.ExchangeRateResolverService;
import com.ihrapanel.backend.exchange.service.ExchangeRateSnapshot;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class MoneySnapshotService {

    private final CurrencyConversionService currencyConversionService;
    private final ExchangeRateResolverService exchangeRateResolverService;

    public MoneySnapshotService(
            CurrencyConversionService currencyConversionService,
            ExchangeRateResolverService exchangeRateResolverService
    ) {
        this.currencyConversionService = currencyConversionService;
        this.exchangeRateResolverService = exchangeRateResolverService;
    }

    public MoneySnapshot createSnapshot(
            BigDecimal amount,
            Currency currency,
            BigDecimal manualRate,
            ExchangeRateSource source,
            Instant transactionTime
    ) {
        if (amount == null) {
    throw new IllegalArgumentException(
            "Tutar boş olamaz."
    );
}

if (currency == null) {
    throw new IllegalArgumentException(
            "Para birimi boş olamaz."
    );
}

if (transactionTime == null) {
    throw new IllegalArgumentException(
            "İşlem zamanı boş olamaz."
    );
}

        ExchangeRateSnapshot rateSnapshot =
                exchangeRateResolverService.resolveRate(
                        currency,
                        Currency.TRY,
                        manualRate,
                        source,
                        transactionTime
                );
                

        BigDecimal tryEquivalent =
                currencyConversionService.toTry(
                        amount,
                        currency,
                        rateSnapshot.rate()
                );

        return new MoneySnapshot(
                amount,
                currency,
                rateSnapshot.rate(),
                tryEquivalent,
                rateSnapshot.source(),
                rateSnapshot.effectiveAt()
        );
    }
}
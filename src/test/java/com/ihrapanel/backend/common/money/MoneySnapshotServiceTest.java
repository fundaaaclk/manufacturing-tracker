package com.ihrapanel.backend.common.money;

import com.ihrapanel.backend.exchange.entity.ExchangeRateSource;
import com.ihrapanel.backend.exchange.service.ExchangeRateResolverService;
import com.ihrapanel.backend.exchange.service.ExchangeRateSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class MoneySnapshotServiceTest {

    private CurrencyConversionService currencyConversionService;
    private ExchangeRateResolverService exchangeRateResolverService;
    private MoneySnapshotService moneySnapshotService;

    @BeforeEach
    void setUp() {
        currencyConversionService =
                new CurrencyConversionService();

        exchangeRateResolverService =
                Mockito.mock(ExchangeRateResolverService.class);

        moneySnapshotService =
                new MoneySnapshotService(
                        currencyConversionService,
                        exchangeRateResolverService
                );
    }

    @Test
    void shouldCreateSnapshotUsingHistoricalRate() {

        Instant transactionTime =
                Instant.parse("2026-09-01T11:20:00Z");

        ExchangeRateSnapshot rateSnapshot =
                new ExchangeRateSnapshot(
                        new BigDecimal("55.350000"),
                        ExchangeRateSource.TCMB,
                        Instant.parse("2026-09-01T10:30:00Z")
                );

        when(
                exchangeRateResolverService.resolveRate(
                        Currency.EUR,
                        Currency.TRY,
                        null,
                        ExchangeRateSource.TCMB,
                        transactionTime
                )
        ).thenReturn(rateSnapshot);

        MoneySnapshot result =
                moneySnapshotService.createSnapshot(
                        new BigDecimal("100000.00"),
                        Currency.EUR,
                        null,
                        ExchangeRateSource.TCMB,
                        transactionTime
                );

        assertEquals(
                new BigDecimal("100000.00"),
                result.originalAmount()
        );

        assertEquals(
                Currency.EUR,
                result.originalCurrency()
        );

        assertEquals(
                new BigDecimal("55.350000"),
                result.fxRate()
        );

        assertEquals(
                new BigDecimal("5535000.00"),
                result.tryEquivalent()
        );

        assertEquals(
                ExchangeRateSource.TCMB,
                result.fxRateSource()
        );

        assertEquals(
                Instant.parse("2026-09-01T10:30:00Z"),
                result.fxRateEffectiveAt()
        );
    }

    @Test
    void shouldCreateSnapshotUsingManualRate() {

        Instant transactionTime =
                Instant.parse("2026-09-01T11:20:00Z");

        ExchangeRateSnapshot rateSnapshot =
                new ExchangeRateSnapshot(
                        new BigDecimal("56.250000"),
                        ExchangeRateSource.MANUAL,
                        transactionTime
                );

        when(
                exchangeRateResolverService.resolveRate(
                        Currency.EUR,
                        Currency.TRY,
                        new BigDecimal("56.250000"),
                        null,
                        transactionTime
                )
        ).thenReturn(rateSnapshot);

        MoneySnapshot result =
                moneySnapshotService.createSnapshot(
                        new BigDecimal("100000.00"),
                        Currency.EUR,
                        new BigDecimal("56.250000"),
                        null,
                        transactionTime
                );

        assertEquals(
                new BigDecimal("56.250000"),
                result.fxRate()
        );

        assertEquals(
                new BigDecimal("5625000.00"),
                result.tryEquivalent()
        );

        assertEquals(
                ExchangeRateSource.MANUAL,
                result.fxRateSource()
        );

        assertEquals(
                transactionTime,
                result.fxRateEffectiveAt()
        );
    }

    @Test
    void shouldCreateSnapshotForTryAmount() {

        Instant transactionTime =
                Instant.parse("2026-09-01T11:20:00Z");

        ExchangeRateSnapshot rateSnapshot =
                new ExchangeRateSnapshot(
                        new BigDecimal("1.000000"),
                        null,
                        null
                );

        when(
                exchangeRateResolverService.resolveRate(
                        Currency.TRY,
                        Currency.TRY,
                        null,
                        null,
                        transactionTime
                )
        ).thenReturn(rateSnapshot);

        MoneySnapshot result =
                moneySnapshotService.createSnapshot(
                        new BigDecimal("1250.50"),
                        Currency.TRY,
                        null,
                        null,
                        transactionTime
                );

        assertEquals(
                new BigDecimal("1250.50"),
                result.originalAmount()
        );

        assertEquals(
                Currency.TRY,
                result.originalCurrency()
        );

        assertEquals(
                new BigDecimal("1.000000"),
                result.fxRate()
        );

        assertEquals(
                new BigDecimal("1250.50"),
                result.tryEquivalent()
        );

        assertNull(result.fxRateSource());
        assertNull(result.fxRateEffectiveAt());
    }



    @Test
void shouldRejectNullAmount() {

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> moneySnapshotService.createSnapshot(
                            null,
                            Currency.EUR,
                            null,
                            ExchangeRateSource.TCMB,
                            Instant.parse("2026-09-01T11:20:00Z")
                    )
            );

    assertEquals(
            "Tutar boş olamaz.",
            exception.getMessage()
    );
}

@Test
void shouldRejectNullCurrency() {

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> moneySnapshotService.createSnapshot(
                            new BigDecimal("100000.00"),
                            null,
                            null,
                            ExchangeRateSource.TCMB,
                            Instant.parse("2026-09-01T11:20:00Z")
                    )
            );

    assertEquals(
            "Para birimi boş olamaz.",
            exception.getMessage()
    );
}

@Test
void shouldRejectNullTransactionTime() {

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> moneySnapshotService.createSnapshot(
                            new BigDecimal("100000.00"),
                            Currency.EUR,
                            null,
                            ExchangeRateSource.TCMB,
                            null
                    )
            );

    assertEquals(
            "İşlem zamanı boş olamaz.",
            exception.getMessage()
    );
}
}
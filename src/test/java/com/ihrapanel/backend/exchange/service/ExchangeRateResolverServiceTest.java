package com.ihrapanel.backend.exchange.service;

import com.ihrapanel.backend.common.money.Currency;
import com.ihrapanel.backend.exchange.entity.ExchangeRate;
import com.ihrapanel.backend.exchange.entity.ExchangeRateSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


class ExchangeRateResolverServiceTest {

    private ExchangeRateService exchangeRateService;
    private ExchangeRateResolverService resolverService;

    @BeforeEach
    void setUp() {
        exchangeRateService =
                Mockito.mock(ExchangeRateService.class);

        resolverService =
                new ExchangeRateResolverService(exchangeRateService);
    }

    @Test
    void shouldUseManualRateWhenProvided() {

        Instant queryTime =
                Instant.parse("2026-09-01T11:20:00Z");

        BigDecimal manualRate =
                new BigDecimal("56.250000");

       ExchangeRateSnapshot result =
        resolverService.resolveRate(
                Currency.EUR,
                Currency.TRY,
                manualRate,
                ExchangeRateSource.TCMB,
                queryTime
        );

assertEquals(
        new BigDecimal("56.250000"),
        result.rate()
);

assertEquals(
        ExchangeRateSource.MANUAL,
        result.source()
);

assertEquals(
        queryTime,
        result.effectiveAt()
);

        verifyNoInteractions(exchangeRateService);
    }

    @Test
    void shouldUseHistoricalRateWhenManualRateIsNotProvided() {

        Instant queryTime =
                Instant.parse("2026-09-01T11:20:00Z");

        ExchangeRate historicalRate =
                new ExchangeRate();

        historicalRate.setBaseCurrency(Currency.EUR);
        historicalRate.setQuoteCurrency(Currency.TRY);
        historicalRate.setRate(
                new BigDecimal("55.350000")
        );
        historicalRate.setEffectiveAt(
                Instant.parse("2026-09-01T10:30:00Z")
        );
        historicalRate.setSource(
                ExchangeRateSource.TCMB
        );

        when(
                exchangeRateService.getLatestRateAt(
                        Currency.EUR,
                        Currency.TRY,
                        ExchangeRateSource.TCMB,
                        queryTime
                )
        ).thenReturn(historicalRate);

       ExchangeRateSnapshot result =
        resolverService.resolveRate(
                Currency.EUR,
                Currency.TRY,
                null,
                ExchangeRateSource.TCMB,
                queryTime
        );

assertEquals(
        new BigDecimal("55.350000"),
        result.rate()
);

assertEquals(
        ExchangeRateSource.TCMB,
        result.source()
);

assertEquals(
        Instant.parse("2026-09-01T10:30:00Z"),
        result.effectiveAt()
);

        verify(exchangeRateService).getLatestRateAt(
                Currency.EUR,
                Currency.TRY,
                ExchangeRateSource.TCMB,
                queryTime
        );
    }


@Test
void shouldRejectZeroManualRate() {

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> resolverService.resolveRate(
                            Currency.EUR,
                            Currency.TRY,
                            BigDecimal.ZERO,
                            ExchangeRateSource.TCMB,
                            Instant.parse("2026-09-01T11:20:00Z")
                    )
            );

    assertEquals(
            "Manuel döviz kuru sıfırdan büyük olmalıdır.",
            exception.getMessage()
    );
}

@Test
void shouldRejectNegativeManualRate() {

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> resolverService.resolveRate(
                            Currency.EUR,
                            Currency.TRY,
                            new BigDecimal("-55.000000"),
                            ExchangeRateSource.TCMB,
                            Instant.parse("2026-09-01T11:20:00Z")
                    )
            );

    assertEquals(
            "Manuel döviz kuru sıfırdan büyük olmalıdır.",
            exception.getMessage()
    );
}


//birim TR-Tr olunda source ve effecitve null olucak
@Test
void shouldReturnOneForTryToTry() {

    ExchangeRateSnapshot result =
            resolverService.resolveRate(
                    Currency.TRY,
                    Currency.TRY,
                    null,
                    ExchangeRateSource.TCMB,
                    Instant.parse("2026-09-01T11:20:00Z")
            );

    assertEquals(
            new BigDecimal("1.000000"),
            result.rate()
    );

    assertNull(result.source());
    assertNull(result.effectiveAt());

    verifyNoInteractions(exchangeRateService);
}



@Test
void shouldRejectNullBaseCurrency() {

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> resolverService.resolveRate(
                            null,
                            Currency.TRY,
                            null,
                            ExchangeRateSource.TCMB,
                            Instant.parse("2026-09-01T11:20:00Z")
                    )
            );

    assertEquals(
            "Kaynak para birimi boş olamaz.",
            exception.getMessage()
    );
}

@Test
void shouldRejectNullQuoteCurrency() {

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> resolverService.resolveRate(
                            Currency.EUR,
                            null,
                            null,
                            ExchangeRateSource.TCMB,
                            Instant.parse("2026-09-01T11:20:00Z")
                    )
            );

    assertEquals(
            "Hedef para birimi boş olamaz.",
            exception.getMessage()
    );
}

@Test
void shouldRejectNullQueryTime() {

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> resolverService.resolveRate(
                            Currency.EUR,
                            Currency.TRY,
                            null,
                            ExchangeRateSource.TCMB,
                            null
                    )
            );

    assertEquals(
            "Kur sorgulama zamanı boş olamaz.",
            exception.getMessage()
    );
}


@Test
void shouldRejectNullSourceWhenHistoricalRateIsRequired() {

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> resolverService.resolveRate(
                            Currency.EUR,
                            Currency.TRY,
                            null,
                            null,
                            Instant.parse("2026-09-01T11:20:00Z")
                    )
            );

    assertEquals(
            "Kur kaynağı boş olamaz.",
            exception.getMessage()
    );
}

@Test
void shouldAllowNullSourceWhenManualRateIsProvided() {

    Instant queryTime =
            Instant.parse("2026-09-01T11:20:00Z");

    ExchangeRateSnapshot result =
            resolverService.resolveRate(
                    Currency.EUR,
                    Currency.TRY,
                    new BigDecimal("56.250000"),
                    null,
                    queryTime
            );

    assertEquals(
            new BigDecimal("56.250000"),
            result.rate()
    );

    assertEquals(
            ExchangeRateSource.MANUAL,
            result.source()
    );

    assertEquals(
            queryTime,
            result.effectiveAt()
    );

    verifyNoInteractions(exchangeRateService);
}

}
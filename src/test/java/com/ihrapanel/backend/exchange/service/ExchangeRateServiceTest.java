package com.ihrapanel.backend.exchange.service;

import com.ihrapanel.backend.common.exception.ConflictException;
import com.ihrapanel.backend.common.exception.ResourceNotFoundException;
import com.ihrapanel.backend.common.money.Currency;
import com.ihrapanel.backend.exchange.entity.ExchangeRate;
import com.ihrapanel.backend.exchange.entity.ExchangeRateSource;
import com.ihrapanel.backend.exchange.repository.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ExchangeRateServiceTest {

    private ExchangeRateRepository exchangeRateRepository;
    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        exchangeRateRepository =
                Mockito.mock(ExchangeRateRepository.class);

        exchangeRateService =
                new ExchangeRateService(exchangeRateRepository);
    }

    @Test
    void shouldReturnLatestRateWhenRateExists() {

        Instant queryTime =
                Instant.parse("2026-09-01T11:20:00Z"); // sorgu anı

        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setBaseCurrency(Currency.EUR);
        exchangeRate.setQuoteCurrency(Currency.TRY);
        exchangeRate.setRate(new BigDecimal("55.350000"));
        exchangeRate.setEffectiveAt(
                Instant.parse("2026-09-01T10:30:00Z")
        );
        exchangeRate.setSource(ExchangeRateSource.TCMB);

        when(
                exchangeRateRepository
                        .findFirstByBaseCurrencyAndQuoteCurrencyAndSourceAndEffectiveAtLessThanEqualOrderByEffectiveAtDesc(
                                Currency.EUR,
                                Currency.TRY,
                                ExchangeRateSource.TCMB,
                                queryTime
                        )
        ).thenReturn(Optional.of(exchangeRate));

        ExchangeRate result =
                exchangeRateService.getLatestRateAt(
                        Currency.EUR,
                        Currency.TRY,
                        ExchangeRateSource.TCMB,
                        queryTime
                );

        assertEquals(
                new BigDecimal("55.350000"),
                result.getRate()
        );
    }

    @Test
    void shouldThrowExceptionWhenRateDoesNotExist() {

        Instant queryTime =
                Instant.parse("2026-09-01T11:20:00Z");

        when(
                exchangeRateRepository
                        .findFirstByBaseCurrencyAndQuoteCurrencyAndSourceAndEffectiveAtLessThanEqualOrderByEffectiveAtDesc(
                                Currency.EUR,
                                Currency.TRY,
                                ExchangeRateSource.TCMB,
                                queryTime
                        )
        ).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> exchangeRateService.getLatestRateAt(
                                Currency.EUR,
                                Currency.TRY,
                                ExchangeRateSource.TCMB,
                                queryTime
                        )
                );

        assertEquals(
                "Belirtilen tarih için döviz kuru bulunamadı.",
                exception.getMessage()
        );
    }


    //kur oluşturma testi
@Test
void shouldCreateExchangeRate() {

    Instant effectiveAt =
            Instant.parse("2026-09-01T10:30:00Z");

    ExchangeRate savedRate = new ExchangeRate();
    savedRate.setBaseCurrency(Currency.EUR);
    savedRate.setQuoteCurrency(Currency.TRY);
    savedRate.setRate(new BigDecimal("55.350000"));
    savedRate.setEffectiveAt(effectiveAt);
    savedRate.setSource(ExchangeRateSource.TCMB);

    when(
            exchangeRateRepository.save(
                    Mockito.any(ExchangeRate.class)
            )
    ).thenReturn(savedRate);

    ExchangeRate result =
            exchangeRateService.createRate(
                    Currency.EUR,
                    Currency.TRY,
                    new BigDecimal("55.350000"),
                    effectiveAt,
                    ExchangeRateSource.TCMB
            );

    assertEquals(Currency.EUR, result.getBaseCurrency());
    assertEquals(Currency.TRY, result.getQuoteCurrency());
    assertEquals(
            new BigDecimal("55.350000"),
            result.getRate()
    );
    assertEquals(effectiveAt, result.getEffectiveAt());
    assertEquals(
            ExchangeRateSource.TCMB,
            result.getSource()
    );
}

@Test
void shouldRejectSameBaseAndQuoteCurrency() {

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> exchangeRateService.createRate(
                            Currency.EUR,
                            Currency.EUR,
                            new BigDecimal("55.350000"),
                            Instant.parse("2026-09-01T10:30:00Z"),
                            ExchangeRateSource.TCMB
                    )
            );

    assertEquals(
            "Kaynak ve hedef para birimi aynı olamaz.",
            exception.getMessage()
    );
}

@Test
void shouldRejectNonPositiveRate() {

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> exchangeRateService.createRate(
                            Currency.EUR,
                            Currency.TRY,
                            BigDecimal.ZERO,
                            Instant.parse("2026-09-01T10:30:00Z"),
                            ExchangeRateSource.TCMB
                    )
            );

    assertEquals(
            "Döviz kuru sıfırdan büyük olmalıdır.",
            exception.getMessage()
    );
}

@Test
void shouldRejectNullBaseCurrency() {

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> exchangeRateService.createRate(
                            null,
                            Currency.TRY,
                            new BigDecimal("55.350000"),
                            Instant.parse("2026-09-01T10:30:00Z"),
                            ExchangeRateSource.TCMB
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
                    () -> exchangeRateService.createRate(
                            Currency.EUR,
                            null,
                            new BigDecimal("55.350000"),
                            Instant.parse("2026-09-01T10:30:00Z"),
                            ExchangeRateSource.TCMB
                    )
            );

    assertEquals(
            "Hedef para birimi boş olamaz.",
            exception.getMessage()
    );
}

@Test
void shouldRejectNullRate() {

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> exchangeRateService.createRate(
                            Currency.EUR,
                            Currency.TRY,
                            null,
                            Instant.parse("2026-09-01T10:30:00Z"),
                            ExchangeRateSource.TCMB
                    )
            );

    assertEquals(
            "Döviz kuru boş olamaz.",
            exception.getMessage()
    );
}

@Test
void shouldRejectNullEffectiveAt() {

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> exchangeRateService.createRate(
                            Currency.EUR,
                            Currency.TRY,
                            new BigDecimal("55.350000"),
                            null,
                            ExchangeRateSource.TCMB
                    )
            );

    assertEquals(
            "Kur zamanı boş olamaz.",
            exception.getMessage()
    );
}

@Test
void shouldRejectNullSource() {

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> exchangeRateService.createRate(
                            Currency.EUR,
                            Currency.TRY,
                            new BigDecimal("55.350000"),
                            Instant.parse("2026-09-01T10:30:00Z"),
                            null
                    )
            );

    assertEquals(
            "Kur kaynağı boş olamaz.",
            exception.getMessage()
    );
}


//duplicate history rate için iki tane aynı tarihte rate olmaısn
@Test
void shouldRejectDuplicateExchangeRate() {

    Instant effectiveAt =
            Instant.parse("2026-09-01T10:30:00Z");

    when(
            exchangeRateRepository
                    .existsByBaseCurrencyAndQuoteCurrencyAndEffectiveAtAndSource(
                            Currency.EUR,
                            Currency.TRY,
                            effectiveAt,
                            ExchangeRateSource.TCMB
                    )
    ).thenReturn(true);

    ConflictException exception =
            assertThrows(
                    ConflictException.class,
                    () -> exchangeRateService.createRate(
                            Currency.EUR,
                            Currency.TRY,
                            new BigDecimal("55.350000"),
                            effectiveAt,
                            ExchangeRateSource.TCMB
                    )
            );

    assertEquals(
            "Bu zaman ve kaynak için döviz kuru zaten mevcut.",
            exception.getMessage()
    );
}

}
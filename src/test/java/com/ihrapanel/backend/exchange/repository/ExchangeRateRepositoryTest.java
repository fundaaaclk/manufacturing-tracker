package com.ihrapanel.backend.exchange.repository;

import com.ihrapanel.backend.common.money.Currency;
import com.ihrapanel.backend.exchange.entity.ExchangeRate;
import com.ihrapanel.backend.exchange.entity.ExchangeRateSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;



// NOT: Projede H2 (embedded test DB) yok, bilinçli olarak gerçek PostgreSQL
// (Railway) kullanıyoruz. Bu yüzden Replace.NONE zorunlu — yoksa Spring
// otomatik olarak H2'ye geçmeye çalışıp bulamadığı için patlıyor.
// DEZAVANTAJ (ileride hatırlanacak): bu testler artık gerçek bir DB
// bağlantısına bağımlı. Local'de PostgreSQL çalışmıyorsa veya Railway
// tüneli kapalıysa bu test de fail olur. CI/CD fazına (roadmap Faz 17)
// geçildiğinde, test ortamında gerçek bir PostgreSQL instance'ının
// nasıl ayağa kaldırılacağını (Docker container, Testcontainers vb.)
// ayrıca çözmemiz gerekecek
@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class ExchangeRateRepositoryTest {

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Test
    void shouldFindLatestRateBeforeOrAtGivenTime() {

        ExchangeRate morning = new ExchangeRate();
        morning.setBaseCurrency(Currency.EUR);
        morning.setQuoteCurrency(Currency.TRY);
        morning.setRate(new BigDecimal("55.100000"));
        morning.setEffectiveAt(
                Instant.parse("2026-09-01T06:00:00Z")
        );
        morning.setSource(ExchangeRateSource.TCMB);

        ExchangeRate afternoon = new ExchangeRate();
        afternoon.setBaseCurrency(Currency.EUR);
        afternoon.setQuoteCurrency(Currency.TRY);
        afternoon.setRate(new BigDecimal("55.350000"));
        afternoon.setEffectiveAt(
                Instant.parse("2026-09-01T10:30:00Z")
        );
        afternoon.setSource(ExchangeRateSource.TCMB);

        exchangeRateRepository.save(morning);
        exchangeRateRepository.save(afternoon);

        Instant transactionTime =
                Instant.parse("2026-09-01T11:20:00Z");

        Optional<ExchangeRate> result =
                exchangeRateRepository
                        .findFirstByBaseCurrencyAndQuoteCurrencyAndSourceAndEffectiveAtLessThanEqualOrderByEffectiveAtDesc(
                                Currency.EUR,
                                Currency.TRY,
                                ExchangeRateSource.TCMB,
                                transactionTime
                        );

        assertTrue(result.isPresent());

        assertEquals(
                new BigDecimal("55.350000"),
                result.get().getRate()
        );
    }
}
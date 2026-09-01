package com.ihrapanel.backend.exchange.service;

import org.springframework.stereotype.Service;
import com.ihrapanel.backend.common.money.Currency;
import com.ihrapanel.backend.exchange.entity.ExchangeRate;
import com.ihrapanel.backend.exchange.entity.ExchangeRateSource;


import java.math.BigDecimal;
import java.time.Instant;

/*bu 
İşlem oluşturuluyor
      |
      v
manual fxRate var mı?
   /       \
 evet      hayır
  |          |
onu kullan  ExchangeRateService'ten
            seçilen source için
            son kuru bul */

@Service
public class ExchangeRateResolverService {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateResolverService(
            ExchangeRateService exchangeRateService
    ) {
        this.exchangeRateService = exchangeRateService;
    }

    public ExchangeRateSnapshot resolveRate(
            Currency baseCurrency,
            Currency quoteCurrency,
            BigDecimal manualRate,
            ExchangeRateSource source,
            Instant queryTime
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

if (queryTime == null) {
    throw new IllegalArgumentException(
            "Kur sorgulama zamanı boş olamaz."
    );
}

        if (baseCurrency == Currency.TRY
        && quoteCurrency == Currency.TRY) {

    return new ExchangeRateSnapshot(
            new BigDecimal("1.000000"),
            null,
            null
    );
}

       if (manualRate != null) {

    if (manualRate.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException(
                "Manuel döviz kuru sıfırdan büyük olmalıdır."
        );
    }

      return new ExchangeRateSnapshot(
                manualRate,
                ExchangeRateSource.MANUAL,
                queryTime
        );
}
if (source == null) {
    throw new IllegalArgumentException(
            "Kur kaynağı boş olamaz."
    );
}

        ExchangeRate exchangeRate =
                exchangeRateService.getLatestRateAt(
                        baseCurrency,
                        quoteCurrency,
                        source,
                        queryTime
                );

        return new ExchangeRateSnapshot(
            exchangeRate.getRate(),
            exchangeRate.getSource(),
            exchangeRate.getEffectiveAt()
    );
    }
}
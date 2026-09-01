package com.ihrapanel.backend.common.money;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CurrencyConversionService {

    public BigDecimal toTry(
            BigDecimal amount,
            Currency currency,
            BigDecimal fxRate
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
  //already TR just put in the right place
        if (currency == Currency.TRY) {
            return amount.setScale(
                    MoneyConstants.MONEY_SCALE,
                    MoneyConstants.ROUNDING_MODE
            );
        }

        if (fxRate == null) {
    throw new IllegalArgumentException(
            "Döviz kuru boş olamaz."
    );
}

if (fxRate.compareTo(BigDecimal.ZERO) <= 0) {
    throw new IllegalArgumentException(
            "Döviz kuru sıfırdan büyük olmalıdır."
    );
}
//if it is euro then multiply with fxRate and set scale
        return amount
                .multiply(fxRate)
                .setScale(
                        MoneyConstants.MONEY_SCALE,
                        MoneyConstants.ROUNDING_MODE
                );
    }
}




/**
 * Sadece currency conversion (kur çevrimi) yapar — saf matematik.
 *
 * Bilinçli tasarım kararı: amount'un pozitif olması gerekip gerekmediği
 * bu servisin sorumluluğunda DEĞİL. Örneğin CheckService için amount
 * her zaman pozitif olmalı, ama CurrencyGainLoss gibi kur farkı kayıtları
 * için negatif değer (kayıp durumunda) anlamlı olabilir. Bu yüzden
 * işaret/yön kontrolü, ilgili domain servisine (Supplier, Check, Ledger vb.)
 * bırakılmıştır. Bu servis yalnızca null kontrolü yapar.
 */
//Test için
package com.ihrapanel.backend.common.money;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class CurrencyConversionServiceTest {

    private final CurrencyConversionService service =
            new CurrencyConversionService();

            //tl ve kur çarpımı gerekince
    @Test
    void shouldConvertEuroToTry() {

        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal fxRate = new BigDecimal("55.000000");

        BigDecimal result = service.toTry(
                amount,
                Currency.EUR,
                fxRate
        );

        assertEquals(
                new BigDecimal("5500.00"),
                result
        );
    }

    //düz tr olunca doğru mu check
    @Test
void shouldReturnSameAmountWhenCurrencyIsTry() {

    BigDecimal amount = new BigDecimal("1250.50");

    BigDecimal result = service.toTry(
            amount,
            Currency.TRY,
            null
    );

    assertEquals(
            new BigDecimal("1250.50"),
            result
    );
}


//biraz daha karmaşık olunca yuvarlama doğru mu?
@Test
void shouldRoundConvertedAmountToTwoDecimals() {

    BigDecimal amount = new BigDecimal("12.34");
    BigDecimal fxRate = new BigDecimal("55.348275");

    BigDecimal result = service.toTry(
            amount,
            Currency.EUR,
            fxRate
    );

    assertEquals(
            new BigDecimal("683.00"),
            result
    );
}


//amount boş gelirse
@Test
void shouldRejectNullAmount() {

    assertThrows(
            IllegalArgumentException.class,
            () -> service.toTry(
                    null,
                    Currency.EUR,
                    new BigDecimal("55.000000")
            )
    );
}


//para birimi boş gelirse
@Test
void shouldRejectNullCurrency() {

    assertThrows(
            IllegalArgumentException.class,
            () -> service.toTry(
                    new BigDecimal("100.00"),
                    null,
                    new BigDecimal("55.000000")
            )
    );
}

//döviz fxRate kuru null gelirse
@Test
void shouldRejectNullFxRateForForeignCurrency() {

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.toTry(
                    new BigDecimal("100.00"),
                    Currency.EUR,
                    null
            )
    );

    assertEquals(
            "Döviz kuru boş olamaz.",
            exception.getMessage()
    );
}

//döviz kuru 0 olursa
@Test
void shouldRejectZeroFxRate() {

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.toTry(
                    new BigDecimal("100.00"),
                    Currency.EUR,
                    BigDecimal.ZERO
            )
    );

    assertEquals(
            "Döviz kuru sıfırdan büyük olmalıdır.",
            exception.getMessage()
    );
}

//döviz kuru 0 dan küçükse
@Test
void shouldRejectNegativeFxRate() {

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.toTry(
                    new BigDecimal("100.00"),
                    Currency.EUR,
                    new BigDecimal("-55.000000")
            )
    );

    assertEquals(
            "Döviz kuru sıfırdan büyük olmalıdır.",
            exception.getMessage()
    );
}



}

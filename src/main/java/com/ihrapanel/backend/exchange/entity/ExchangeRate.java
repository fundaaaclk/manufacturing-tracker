package com.ihrapanel.backend.exchange.entity;

import com.ihrapanel.backend.common.money.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


//tablomuzun adı  exchange_rates, ve "base_currency","quote_currency","effective_at","source" alanları
// unique olacak şekilde constraint ekledik ve buna da loglarda net görebilmek için uk_exchange_rate_pair_time_source
//  adını verdik
                               
@Entity
@Table(
        name = "exchange_rates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_exchange_rate_pair_time_source",
                        columnNames = {
                                "base_currency",
                                "quote_currency",
                                "effective_at",
                                "source"
                        }
                )
        }
)

//hepsi içi getter setter fonksiyonu üretilir
@Getter
@Setter
@NoArgsConstructor //constructor üretiriyor JPA için zorunlu
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    //baseCurrency  ve quoteCurrency alanları Currency enum tipinde ve veritabanında string olarak saklanacak (lenght=3 de enumlar 3 karakterli)
    //baseCurrency = 1 birim neyin(Euro )
    //quoteCurrency = kaç birim karşılıgı (Türk Lirası)
    @Enumerated(EnumType.STRING)
    @Column(name = "base_currency", nullable = false, length = 3)
    private Currency baseCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "quote_currency", nullable = false, length = 3)
    private Currency quoteCurrency;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal rate;

    //bu kur, hangi an için geçerliydi
    @Column(name = "effective_at", nullable = false)
    private Instant effectiveAt;

//manual or tcmb den gelen kur bilgisi
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExchangeRateSource source;

    //burda update=false su demek bu variable update edilemez, sadece insert edilebilir.
    //  createdAt alanı veritabanına kaydedildikten sonra değiştirilemez.
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;


//bu satır DB'ye ne zaman yazıldı
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

/*baseCurrency = EUR
quoteCurrency = TRY
rate = 55.348275

effectiveAt = kurun geçerli olduğu zaman
source = TCMB
createdAt = bizim sisteme kaydedildiği zaman */

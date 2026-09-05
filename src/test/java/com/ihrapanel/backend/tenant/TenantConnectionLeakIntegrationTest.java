package com.ihrapanel.backend.tenant;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TenantConnectionLeakIntegrationTest {

    private static final UUID COMPANY_A =
            UUID.fromString("11111111-1111-1111-1111-111111111111");


    private static final UUID COMPANY_B =
        UUID.fromString("22222222-2222-2222-2222-222222222222");
    @Autowired
    private TenantDatabaseContext tenantDatabaseContext;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void tenantDatabaseSettingShouldDisappearAfterTransactionEnds() {

        TenantContext.setCompanyId(COMPANY_A);

        transactionTemplate.executeWithoutResult(status -> {

            tenantDatabaseContext.applyCurrentTenant();

            Object insideTransaction = entityManager
                    .createNativeQuery("""
                            SELECT current_setting(
                                'app.current_company_id',
                                true
                            )
                            """)
                    .getSingleResult();

            assertEquals(
                    COMPANY_A.toString(),
                    insideTransaction.toString()
            );
        });

        TenantContext.clear();

        Object afterTransaction =
                transactionTemplate.execute(status ->
                        entityManager
                                .createNativeQuery("""
                                        SELECT current_setting(
                                            'app.current_company_id',
                                            true
                                        )
                                        """)
                                .getSingleResult()
                );

        assertTrue(
                afterTransaction == null
                        || afterTransaction.toString().isBlank()
        );
    }


@Test
void companyAContextShouldNotLeakIntoCompanyBTransaction() {

    // 1️⃣ Company A transaction
    TenantContext.setCompanyId(COMPANY_A);

    transactionTemplate.executeWithoutResult(status -> {

        tenantDatabaseContext.applyCurrentTenant();

        Object companyASetting = entityManager
                .createNativeQuery("""
                        SELECT current_setting(
                            'app.current_company_id',
                            true
                        )
                        """)
                .getSingleResult();

        assertEquals(
                COMPANY_A.toString(),
                companyASetting.toString()
        );
    });

    // Request A bitti
    TenantContext.clear();


    // 2️⃣ Şimdi Company B geliyor
    TenantContext.setCompanyId(COMPANY_B);

    transactionTemplate.executeWithoutResult(status -> {

        tenantDatabaseContext.applyCurrentTenant();

        Object companyBSetting = entityManager
                .createNativeQuery("""
                        SELECT current_setting(
                            'app.current_company_id',
                            true
                        )
                        """)
                .getSingleResult();

        assertEquals(
                COMPANY_B.toString(),
                companyBSetting.toString()
        );

        // Özellikle A kalmadığını da doğruluyoruz.
        assertNotEquals(
                COMPANY_A.toString(),
                companyBSetting.toString()
        );
    });
}


@Test
void tenantContextShouldNotLeakAfterClear() {

    // Request A başladı
    TenantContext.setCompanyId(COMPANY_A);

    assertTrue(TenantContext.isSet());
    assertEquals(
            COMPANY_A,
            TenantContext.getCompanyId()
    );

    // Request A bitti
    TenantContext.clear();

    // Aynı thread başka request için tekrar kullanılsa bile
    // eski tenant kalmamalı.
    assertFalse(TenantContext.isSet());
    assertNull(TenantContext.getCompanyIdOrNull());

    assertThrows(
            IllegalStateException.class,
            TenantContext::getCompanyId
    );
}

}
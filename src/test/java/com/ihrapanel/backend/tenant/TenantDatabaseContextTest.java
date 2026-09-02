package com.ihrapanel.backend.tenant;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TenantDatabaseContextTest {


    @Autowired
private org.springframework.transaction.support.TransactionTemplate transactionTemplate; 

    @Autowired
    private TenantDatabaseContext tenantDatabaseContext;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }


    
    @Test
    @Transactional
    void shouldApplyCurrentTenantToPostgresTransaction() {

        UUID companyId = UUID.randomUUID();

        // Java tarafındaki tenant
        TenantContext.setCompanyId(companyId);

        // Aynı transaction içindeki PostgreSQL connection'ına aktar.
        tenantDatabaseContext.applyCurrentTenant();

        // PostgreSQL'den tekrar oku.
        String databaseCompanyId = (String) entityManager
                .createNativeQuery(
                        """
                        SELECT current_setting(
                            'app.current_company_id',
                            true
                        )
                        """
                )
                .getSingleResult();

        assertEquals(
                companyId.toString(),
                databaseCompanyId
        );
    }


//yanlışlıkla transaction başlamadan
    //  DB'ye tenant context vermeye çalışırsak sistem “tamamdır” deyip sessizce devam etmeyecek
    @Test
void shouldFailWhenCalledWithoutTransaction() {

    UUID companyId = UUID.randomUUID();

    TenantContext.setCompanyId(companyId);

    assertThrows(
            org.springframework.transaction.IllegalTransactionStateException.class,
            () -> tenantDatabaseContext.applyCurrentTenant()
    );
}


@Test
void shouldNotLeakDatabaseTenantContextAfterTransactionEnds() {

    UUID companyA = UUID.randomUUID();

    TenantContext.setCompanyId(companyA);

    // 1. Transaction:
    // Company A bilgisini PostgreSQL transaction'ına yazıyoruz.
    transactionTemplate.executeWithoutResult(status -> {

        tenantDatabaseContext.applyCurrentTenant();

        String databaseCompanyId = (String) entityManager
                .createNativeQuery(
                        """
                        SELECT current_setting(
                            'app.current_company_id',
                            true
                        )
                        """
                )
                .getSingleResult();

        assertEquals(
                companyA.toString(),
                databaseCompanyId
        );
    });

    // Java tarafını da request bitmiş gibi temizliyoruz.
    TenantContext.clear();

    // 2. Yeni transaction:
    // Eski Company A değeri kalmış mı kontrol ediyoruz.
    transactionTemplate.executeWithoutResult(status -> {

        String databaseCompanyId = (String) entityManager
                .createNativeQuery(
                        """
                        SELECT current_setting(
                            'app.current_company_id',
                            true
                        )
                        """
                )
                .getSingleResult();

        assertTrue(
                databaseCompanyId == null || databaseCompanyId.isBlank()
        );
    });
}


@Test
void shouldSwitchTenantBetweenSeparateTransactions() {

    UUID companyA = UUID.randomUUID();
    UUID companyB = UUID.randomUUID();

    // Transaction A
    TenantContext.setCompanyId(companyA);

    transactionTemplate.executeWithoutResult(status -> {

        tenantDatabaseContext.applyCurrentTenant();

        String databaseCompanyId = (String) entityManager
                .createNativeQuery(
                        """
                        SELECT current_setting(
                            'app.current_company_id',
                            true
                        )
                        """
                )
                .getSingleResult();

        assertEquals(
                companyA.toString(),
                databaseCompanyId
        );
    });

    TenantContext.clear();

    // Transaction B
    TenantContext.setCompanyId(companyB);

    transactionTemplate.executeWithoutResult(status -> {

        tenantDatabaseContext.applyCurrentTenant();

        String databaseCompanyId = (String) entityManager
                .createNativeQuery(
                        """
                        SELECT current_setting(
                            'app.current_company_id',
                            true
                        )
                        """
                )
                .getSingleResult();

        assertEquals(
                companyB.toString(),
                databaseCompanyId
        );

        assertNotEquals(
                companyA.toString(),
                databaseCompanyId
        );
    });

    TenantContext.clear();
}


}
package com.ihrapanel.backend.tenant;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TenantRlsIntegrationTest {

    private static final UUID COMPANY_A =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID COMPANY_B =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private TenantRlsTestService tenantRlsTestService;

   @Autowired
    private TenantDatabaseContext tenantDatabaseContext;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;
  
    @BeforeEach
    void setup() {

        TenantContext.clear();

        /*
         * Test verisini RLS dışında hazırlıyoruz.
         * Test şu an postgres/migrator gibi yetkili bağlantıyla değil,
         * runtime datasource ile çalıştığı için company kayıtlarının
         * önceden DB'de mevcut olduğunu kabul ediyoruz.
         */
    }

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void companyAShouldSeeOnlyCompanyAData() {

        TenantContext.setCompanyId(COMPANY_A);

        List<TenantRlsTestEntity> results =
                tenantRlsTestService.findAllForCurrentTenant();

        assertFalse(results.isEmpty());

        assertTrue(
                results.stream()
                        .allMatch(row -> COMPANY_A.equals(row.getCompanyId()))
        );

        assertTrue(
                results.stream()
                        .anyMatch(row -> "A kaydi".equals(row.getName()))
        );

        assertFalse(
                results.stream()
                        .anyMatch(row -> COMPANY_B.equals(row.getCompanyId()))
        );
    }

    @Test
    void companyBShouldSeeOnlyCompanyBData() {

        TenantContext.setCompanyId(COMPANY_B);

        List<TenantRlsTestEntity> results =
                tenantRlsTestService.findAllForCurrentTenant();

        assertFalse(results.isEmpty());

        assertTrue(
                results.stream()
                        .allMatch(row -> COMPANY_B.equals(row.getCompanyId()))
        );

        assertTrue(
                results.stream()
                        .anyMatch(row -> "B kaydi".equals(row.getName()))
        );

        assertFalse(
                results.stream()
                        .anyMatch(row -> COMPANY_A.equals(row.getCompanyId()))
        );
    }

    @Test
    void companyAShouldNotInsertDataForCompanyB() {

        TenantContext.setCompanyId(COMPANY_A);

        TenantRlsTestEntity maliciousRow =
                new TenantRlsTestEntity(
                        UUID.randomUUID(),
                        COMPANY_B,
                        "Cross tenant attack"
                );

        assertThrows(
                RuntimeException.class,
                () -> tenantRlsTestService.saveForCurrentTenant(maliciousRow)
        );
    }

    @Test
    void companyAShouldInsertItsOwnData() {

        TenantContext.setCompanyId(COMPANY_A);

        UUID id = UUID.randomUUID();

        TenantRlsTestEntity validRow =
                new TenantRlsTestEntity(
                        id,
                        COMPANY_A,
                        "Automatic RLS test"
                );

        TenantRlsTestEntity saved =
                tenantRlsTestService.saveForCurrentTenant(validRow);

        assertEquals(COMPANY_A, saved.getCompanyId());
        assertEquals(id, saved.getId());

        /*
         * Test verisini sonra temizlemek için ayrı transaction.
         */
        transactionTemplate.executeWithoutResult(status -> {
            tenantDatabaseContext.applyCurrentTenant();
            entityManager.createNativeQuery(
                    "DELETE FROM tenant_rls_test WHERE id = :id"
            )
            .setParameter("id", id)
            .executeUpdate();
        });
    }



 

/*Spring/JPA
   ↓
ihrapanel_app
   ↓
TenantDatabaseContext
   ↓
app.current_company_id
   ↓
PostgreSQL RLS
   ↓
Company A ≠ Company B 🔐 */
@Test
void applicationShouldUseRuntimeDatabaseRole() {

    transactionTemplate.executeWithoutResult(status -> {

        Object currentUser = entityManager
                .createNativeQuery("SELECT current_user")
                .getSingleResult();

        Object sessionUser = entityManager
                .createNativeQuery("SELECT session_user")
                .getSingleResult();

        assertEquals("ihrapanel_app", currentUser.toString());
        assertEquals("ihrapanel_app", sessionUser.toString());
    });
}

@Test
void companyAShouldNotUpdateCompanyBData() {

    TenantContext.setCompanyId(COMPANY_A);

    int updatedRows = tenantRlsTestService.updateName(
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            "HACKED BY COMPANY A"
    );

    assertEquals(0, updatedRows);
}


@Test
void companyAShouldNotDeleteCompanyBData() {

    TenantContext.setCompanyId(COMPANY_A);

    int deletedRows = tenantRlsTestService.deleteById(
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
    );

    assertEquals(0, deletedRows);
}

@Test
void companyAShouldUpdateItsOwnData() {

    TenantContext.setCompanyId(COMPANY_A);

    int updatedRows = tenantRlsTestService.updateName(
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            "A kaydi updated"
    );

    assertEquals(1, updatedRows);

    // Eski haline döndür
    int restoredRows = tenantRlsTestService.updateName(
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            "A kaydi"
    );

    assertEquals(1, restoredRows);
}


@Test
void companyAShouldDeleteItsOwnData() {

    TenantContext.setCompanyId(COMPANY_A);

    UUID id = UUID.randomUUID();

    TenantRlsTestEntity temporaryRow =
            new TenantRlsTestEntity(
                    id,
                    COMPANY_A,
                    "Temporary delete test"
            );

    tenantRlsTestService.saveForCurrentTenant(temporaryRow);

    int deletedRows =
            tenantRlsTestService.deleteById(id);

    assertEquals(1, deletedRows);

    List<TenantRlsTestEntity> remaining =
            tenantRlsTestService.findAllForCurrentTenant();

    assertTrue(
            remaining.stream()
                    .noneMatch(row -> id.equals(row.getId()))
    );
}

/* 
@Test
void companyAShouldNotAccessCompanyBRowByKnownId() {

    TenantContext.setCompanyId(COMPANY_A);

    TenantRlsTestEntity result =
            tenantRlsTestService.findById(COMPANY_B_ROW);

    assertNull(result);
}*/


}
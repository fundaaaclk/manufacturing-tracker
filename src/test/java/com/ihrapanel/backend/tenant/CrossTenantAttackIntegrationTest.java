package com.ihrapanel.backend.tenant;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CrossTenantAttackIntegrationTest {



    private static final UUID COMPANY_A =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID COMPANY_B =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID COMPANY_B_ROW =
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Autowired
    private TenantRlsTestService tenantRlsTestService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
private TenantDatabaseContext tenantDatabaseContext;

    @Autowired
private TenantRlsTestRepository tenantRlsTestRepository; 

    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }
@Test
void companyAShouldNotAccessCompanyBRowByKnownId() {

    TenantContext.setCompanyId(COMPANY_A);

    TenantRlsTestEntity result =
            tenantRlsTestService.findById(COMPANY_B_ROW);

    assertNull(result);
}

@Test
void companyAShouldNotInsertRowWithCompanyBId() {

    // Gerçek authenticated tenant = Company A
    TenantContext.setCompanyId(COMPANY_A);

    // Saldırgan request/payload içindeki companyId'yi
    // Company B olarak değiştirmiş gibi davranıyoruz.
    TenantRlsTestEntity maliciousRow =
            new TenantRlsTestEntity(
                    UUID.randomUUID(),
                    COMPANY_B,
                    "Fake companyId attack"
            );

    assertThrows(
            RuntimeException.class,
            () -> tenantRlsTestService.saveForCurrentTenant(maliciousRow)
    );
}

@Test
void companyAShouldNotReferenceCompanyBParent() {

    UUID childId = UUID.randomUUID();

    assertThrows(
            RuntimeException.class,
            () -> transactionTemplate.executeWithoutResult(status -> {

                entityManager.createNativeQuery("""
                        INSERT INTO tenant_rls_child_test
                            (id, company_id, parent_id, name)
                        VALUES
                            (:id, :companyId, :parentId, :name)
                        """)
                        .setParameter("id", childId)
                        .setParameter("companyId", COMPANY_A)
                        .setParameter(
                                "parentId",
                                UUID.fromString(
                                        "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
                                )
                        )
                        .setParameter(
                                "name",
                                "Cross tenant FK attack"
                        )
                        .executeUpdate();
            })
    );
}


@Test
void requestWithoutTenantContextShouldFailClosed() {

    TenantContext.clear();

    assertThrows(
            IllegalStateException.class,
            () -> tenantRlsTestService.findAllForCurrentTenant()
    );
}

@Test
void directRepositoryAccessWithoutDatabaseTenantShouldReturnNoData() {

    // Java tarafında A var...
    TenantContext.setCompanyId(COMPANY_A);

    // ...ama Service'i bypass ediyoruz.
    // Dolayısıyla applyCurrentTenant() çalışmayacak.
    List<TenantRlsTestEntity> results =
            transactionTemplate.execute(status ->
                    tenantRlsTestRepository.findAll()
            );

    assertNotNull(results);
    assertTrue(results.isEmpty());
}
@Test
void directRepositoryAccessShouldStillNotExposeOtherTenant() {

    TenantContext.setCompanyId(COMPANY_A);

    TenantRlsTestEntity result =
            transactionTemplate.execute(status -> {

                // DB transaction'ını Company A olarak ayarla
                tenantDatabaseContext.applyCurrentTenant();

                // Service'i bypass ederek doğrudan repository
                return tenantRlsTestRepository
                        .findById(COMPANY_B_ROW)
                        .orElse(null);
            });

    assertNull(result);
}

}
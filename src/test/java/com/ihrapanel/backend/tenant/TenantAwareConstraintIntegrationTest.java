package com.ihrapanel.backend.tenant;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import org.hibernate.exception.ConstraintViolationException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TenantAwareConstraintIntegrationTest {

    private static final UUID COMPANY_A =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID COMPANY_B_PARENT =
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private static final UUID COMPANY_A_PARENT =
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void sameTenantRelationshipShouldBeAllowed() {

        UUID childId = UUID.randomUUID();

        transactionTemplate.executeWithoutResult(status -> {

            entityManager.createNativeQuery("""
                    INSERT INTO tenant_rls_child_test
                        (id, company_id, parent_id, name)
                    VALUES
                        (:id, :companyId, :parentId, :name)
                    """)
                    .setParameter("id", childId)
                    .setParameter("companyId", COMPANY_A)
                    .setParameter("parentId", COMPANY_A_PARENT)
                    .setParameter("name", "Valid same-tenant relation")
                    .executeUpdate();

            // Test transaction'ını geri al:
            // DB'de test verisi bırakmayalım.
            status.setRollbackOnly();
        });
    }

  @Test
void crossTenantRelationshipShouldBeRejected() {

    UUID childId = UUID.randomUUID();

    assertThrows(
            ConstraintViolationException.class,
            () -> transactionTemplate.executeWithoutResult(status -> {

                entityManager.createNativeQuery("""
                        INSERT INTO tenant_rls_child_test
                            (id, company_id, parent_id, name)
                        VALUES
                            (:id, :companyId, :parentId, :name)
                        """)
                        .setParameter("id", childId)
                        .setParameter("companyId", COMPANY_A)
                        .setParameter("parentId", COMPANY_B_PARENT)
                        .setParameter("name", "Cross-tenant attack")
                        .executeUpdate();
            })
    );
}
}
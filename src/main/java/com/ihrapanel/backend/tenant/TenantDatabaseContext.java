package com.ihrapanel.backend.tenant;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
//Java TenantContext → PostgreSQL transaction context köprüsünü
@Component
public class TenantDatabaseContext {

    private final EntityManager entityManager;

    public TenantDatabaseContext(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(propagation = Propagation.MANDATORY) //Transaction varsa  → devam ✅ Transaction yoksa        → hata ❌

    public void applyCurrentTenant() {

        UUID companyId = TenantContext.getCompanyId();

        entityManager.createNativeQuery(
                """
                SELECT set_config(
                    'app.current_company_id',
                    :companyId,
                    true
                )
                """
        )//yalnızca mevcut transaction boyunca geçerli demek.
        .setParameter("companyId", companyId.toString())
        .getSingleResult();
    }
}
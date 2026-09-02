//tenant_rls_test tablomuz zaten var. Şimdi Java tarafında onu temsil edecek küçük bir entity


package com.ihrapanel.backend.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "tenant_rls_test")
public class TenantRlsTestEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private String name;

    protected TenantRlsTestEntity() {
    }

    public TenantRlsTestEntity(
            UUID id,
            UUID companyId,
            String name
    ) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getName() {
        return name;
    }
}

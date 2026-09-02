package com.ihrapanel.backend.tenant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TenantRlsTestService {

    private final TenantDatabaseContext tenantDatabaseContext;
    private final TenantRlsTestRepository repository;

    public TenantRlsTestService(
            TenantDatabaseContext tenantDatabaseContext,
            TenantRlsTestRepository repository
    ) {
        this.tenantDatabaseContext = tenantDatabaseContext;
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TenantRlsTestEntity> findAllForCurrentTenant() {

        tenantDatabaseContext.applyCurrentTenant();

        return repository.findAll();
    }

    @Transactional
    public TenantRlsTestEntity saveForCurrentTenant(
            TenantRlsTestEntity entity
    ) {

        tenantDatabaseContext.applyCurrentTenant();

        return repository.saveAndFlush(entity);
    }
}


/*@Transactional
      ↓
applyCurrentTenant()
      ↓
repository
      ↓
PostgreSQL RLS */
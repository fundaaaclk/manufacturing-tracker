package com.ihrapanel.backend.tenant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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
   //CRUD işlemi testi 
@Transactional
public int updateName(UUID id, String newName) {

    tenantDatabaseContext.applyCurrentTenant();

    return repository.updateNameById(id, newName);
}
  //CRUD işlemi testi 
@Transactional
public int deleteById(UUID id) {

    tenantDatabaseContext.applyCurrentTenant();

    return repository.deleteDirectlyById(id);
}

@Transactional(readOnly = true)
public TenantRlsTestEntity findById(UUID id) {

    tenantDatabaseContext.applyCurrentTenant();

    return repository.findById(id)
            .orElse(null);
}



}


/*@Transactional
      ↓
applyCurrentTenant()
      ↓
repository
      ↓
PostgreSQL RLS */




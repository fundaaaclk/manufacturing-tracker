package com.ihrapanel.backend.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantRlsTestRepository
        extends JpaRepository<TenantRlsTestEntity, UUID> {
}
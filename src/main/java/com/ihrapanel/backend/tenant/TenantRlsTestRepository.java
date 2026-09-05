package com.ihrapanel.backend.tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TenantRlsTestRepository
        extends JpaRepository<TenantRlsTestEntity, UUID> {

    // UPDATE
    @Modifying
    @Query("""
            UPDATE TenantRlsTestEntity t
            SET t.name = :name
            WHERE t.id = :id
            """)
    int updateNameById(
            @Param("id") UUID id,
            @Param("name") String name
    );

    // DELETE
    @Modifying
    @Query("""
            DELETE FROM TenantRlsTestEntity t
            WHERE t.id = :id
            """)
    int deleteDirectlyById(
            @Param("id") UUID id
    );
}
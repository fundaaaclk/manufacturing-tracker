package com.ihrapanel.backend.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void shouldSetAndGetCompanyId() {
        UUID companyId = UUID.randomUUID();

        TenantContext.setCompanyId(companyId);

        assertEquals(companyId, TenantContext.getCompanyId());
        assertTrue(TenantContext.isSet());
    }

    @Test
    void shouldClearCompanyId() {
        UUID companyId = UUID.randomUUID();

        TenantContext.setCompanyId(companyId);
        TenantContext.clear();

        assertFalse(TenantContext.isSet());
        assertNull(TenantContext.getCompanyIdOrNull());
    }

    @Test
    void shouldFailClosedWhenTenantIsMissing() {
        assertThrows(
                IllegalStateException.class,
                TenantContext::getCompanyId
        );
    }

    @Test
    void shouldRejectNullCompanyId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TenantContext.setCompanyId(null)
        );
    }

    @Test
    void shouldNotLeakTenantBetweenRequests() {
        UUID companyA = UUID.randomUUID();
        UUID companyB = UUID.randomUUID();

        // Request A
        TenantContext.setCompanyId(companyA);
        assertEquals(companyA, TenantContext.getCompanyId());

        // Request A bitti
        TenantContext.clear();

        assertFalse(TenantContext.isSet());

        // Request B
        TenantContext.setCompanyId(companyB);
        assertEquals(companyB, TenantContext.getCompanyId());
        assertNotEquals(companyA, TenantContext.getCompanyId());
    }
}
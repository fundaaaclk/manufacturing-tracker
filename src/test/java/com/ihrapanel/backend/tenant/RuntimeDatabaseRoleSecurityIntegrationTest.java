package com.ihrapanel.backend.tenant;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RuntimeDatabaseRoleSecurityIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void applicationShouldUseRuntimeDatabaseRole() {

        transactionTemplate.executeWithoutResult(status -> {

            Object currentUser = entityManager
                    .createNativeQuery("SELECT current_user")
                    .getSingleResult();

            Object sessionUser = entityManager
                    .createNativeQuery("SELECT session_user")
                    .getSingleResult();

            assertEquals(
                    "ihrapanel_app",
                    currentUser.toString()
            );

            assertEquals(
                    "ihrapanel_app",
                    sessionUser.toString()
            );
        });
    }

    @Test
void runtimeRoleShouldNotHaveDangerousDatabasePrivileges() {

    transactionTemplate.executeWithoutResult(status -> {

        Object[] role = (Object[]) entityManager
                .createNativeQuery("""
                        SELECT
                            rolsuper,
                            rolcreatedb,
                            rolcreaterole,
                            rolbypassrls
                        FROM pg_roles
                        WHERE rolname = current_user
                        """)
                .getSingleResult();

        // SUPERUSER olmamalı
        assertEquals(false, role[0]);

        // Database oluşturamamalı
        assertEquals(false, role[1]);

        // Role/User oluşturamamalı
        assertEquals(false, role[2]);

        // RLS'i bypass edememeli
        assertEquals(false, role[3]);
    });
}





@Test
void runtimeRoleShouldNotBeAbleToAlterApplicationTables() {

    assertThrows(
            RuntimeException.class,
            () -> transactionTemplate.executeWithoutResult(status -> {

                entityManager.createNativeQuery("""
                        ALTER TABLE tenant_rls_test
                        ADD COLUMN runtime_attack_test VARCHAR(10)
                        """)
                        .executeUpdate();
            })
    );
}

@Test
void runtimeRoleShouldNotBeAbleToDisableRls() {

    assertThrows(
            RuntimeException.class,
            () -> transactionTemplate.executeWithoutResult(status -> {

                entityManager.createNativeQuery("""
                        ALTER TABLE tenant_rls_test
                        DISABLE ROW LEVEL SECURITY
                        """)
                        .executeUpdate();
            })
    );
}

@Test
void runtimeRoleShouldNotBeAbleToModifyFlywayHistory() {

    assertThrows(
            RuntimeException.class,
            () -> transactionTemplate.executeWithoutResult(status -> {

                entityManager.createNativeQuery("""
                        DELETE FROM flyway_schema_history
                        WHERE installed_rank = -999
                        """)
                        .executeUpdate();
            })
    );
}
}
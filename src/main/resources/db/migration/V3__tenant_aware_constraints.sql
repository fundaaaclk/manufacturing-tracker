ALTER TABLE tenant_rls_test
    ADD CONSTRAINT uq_tenant_rls_test_company_id_id
    UNIQUE (company_id, id);
CREATE TABLE tenant_rls_test (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,

    CONSTRAINT fk_tenant_rls_test_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id)
);

CREATE INDEX idx_tenant_rls_test_company_id
    ON tenant_rls_test(company_id);

ALTER TABLE tenant_rls_test
    ENABLE ROW LEVEL SECURITY;

ALTER TABLE tenant_rls_test
    FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_rls_test_isolation_policy
    ON tenant_rls_test
    USING (
        company_id = NULLIF(
            current_setting('app.current_company_id', true),
            ''
        )::UUID
    )
    WITH CHECK (
        company_id = NULLIF(
            current_setting('app.current_company_id', true),
            ''
        )::UUID
    );
-- (company_id, id) unique index already supports
-- queries starting with company_id, so the old single-column
-- index is redundant on this test table.
DROP INDEX IF EXISTS idx_tenant_rls_test_company_id;

-- Foreign keys do not automatically create an index
-- on the referencing/child columns in PostgreSQL.
CREATE INDEX idx_tenant_rls_child_company_parent
    ON tenant_rls_child_test(company_id, parent_id);
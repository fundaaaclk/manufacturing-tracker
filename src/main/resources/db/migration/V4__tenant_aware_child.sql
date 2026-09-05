CREATE TABLE tenant_rls_child_test (
    id UUID PRIMARY KEY,

    company_id UUID NOT NULL,

    parent_id UUID NOT NULL,

    name VARCHAR(255) NOT NULL,

    CONSTRAINT fk_tenant_rls_child_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT fk_tenant_rls_child_parent
        FOREIGN KEY (company_id, parent_id)
        REFERENCES tenant_rls_test(company_id, id)
);
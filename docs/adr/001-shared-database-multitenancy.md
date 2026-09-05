# ADR-001: Shared Database Multi-Tenancy with PostgreSQL RLS

- Status: Accepted
- Date: 2026-09-05
- Project: İmalat & İhracat Takip Uygulaması

## 1. Context

The application is a multi-tenant manufacturing and export tracking
platform.

Multiple companies use the same application, but each company must only
be able to access its own operational data.

Examples of tenant-owned data include:

- products
- warehouses
- stock movements
- suppliers
- purchases
- production orders
- production costs
- checks
- VAT records
- exports
- receivables
- financial ledger entries

Some data is platform-owned rather than tenant-owned.

Examples:

- companies
- users
- exchange rates

The initial architecture considered using a separate PostgreSQL database
for every company.

For the current expected scale and development model, this would introduce
significant operational complexity:

- separate database provisioning
- multiple migration targets
- multiple connection pools
- more complicated backups
- more complicated monitoring
- more complicated tenant onboarding
- higher operational overhead

The project is currently developed and operated by a small team and is
expected to initially serve tens of companies rather than requiring
dedicated infrastructure for every tenant.

Therefore, database-per-tenant is not used for the initial architecture.

---

## 2. Decision

The application will use:

**Shared PostgreSQL database + company_id + PostgreSQL Row Level Security.**

Tenant-owned tables must contain:

    company_id UUID NOT NULL

The authenticated tenant identity is derived from the authenticated user
and JWT.

The request-level tenant identity is stored in:

    TenantContext

Before accessing tenant-owned repositories, the service starts a
transaction and applies the tenant to PostgreSQL using:

    TenantDatabaseContext.applyCurrentTenant()

The database tenant context is set using:

    set_config(
        'app.current_company_id',
        companyId,
        true
    )

The third argument is `true`, making the setting transaction-local.

---

## 3. Tenant Identity Flow

The expected request flow is:

    HTTP Request
        ↓
    JWT
        ↓
    JwtAuthenticationFilter
        ↓
    Validate userId + companyId against database
        ↓
    AuthenticatedUser
        ↓
    TenantContext
        ↓
    @Transactional Service
        ↓
    TenantDatabaseContext
        ↓
    app.current_company_id
        ↓
    PostgreSQL RLS
        ↓
    Tenant-owned table

The client is not considered a trusted source for tenant identity.

A company_id supplied inside a request payload must never override the
authenticated tenant identity.

---

## 4. TenantContext

TenantContext stores the current company using ThreadLocal.

It must fail closed.

If tenant information is required but unavailable:

    TenantContext.getCompanyId()

must throw an exception instead of silently continuing.

TenantContext must always be cleared when the request finishes.

JwtAuthenticationFilter therefore uses:

    try {
        filterChain.doFilter(request, response);
    } finally {
        TenantContext.clear();
    }

This cleanup must also occur when request processing throws an exception.

---

## 5. Database Tenant Context

Java TenantContext alone does not provide database isolation.

TenantDatabaseContext transfers the authenticated tenant identity into
the current PostgreSQL transaction.

It requires an existing transaction:

    @Transactional(propagation = Propagation.MANDATORY)

This prevents tenant database context from being applied outside a
transaction.

The PostgreSQL setting is transaction-local to prevent tenant information
from leaking through pooled database connections.

---

## 6. Row Level Security

Every tenant-owned table must use PostgreSQL Row Level Security.

Required pattern:

    ALTER TABLE <table>
        ENABLE ROW LEVEL SECURITY;

    ALTER TABLE <table>
        FORCE ROW LEVEL SECURITY;

Policies must compare the row company_id with the current database tenant.

Example:

    company_id = NULLIF(
        current_setting('app.current_company_id', true),
        ''
    )::UUID

Both USING and WITH CHECK must be defined when appropriate.

Example:

    CREATE POLICY <policy_name>
        ON <table>
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

Missing tenant context must not expose all rows.

The system follows a fail-closed model.

---

## 7. Application Filtering Is Not a Security Boundary

Queries such as:

    WHERE company_id = :companyId

may still be useful for clarity and query design.

However, application-level filtering is not considered the primary
tenant security boundary.

A developer may accidentally write:

    repository.findAll()

or:

    repository.findById(id)

without adding a company filter.

PostgreSQL RLS must still prevent cross-tenant access.

Automated integration tests verify this behavior.

---

## 8. Tenant-Aware Relationships

Foreign keys between tenant-owned tables must prevent cross-tenant
relationships.

A normal foreign key such as:

    supplier_id -> suppliers.id

is not sufficient as the only tenant integrity protection.

The preferred pattern is:

Parent:

    UNIQUE (company_id, id)

Child:

    FOREIGN KEY (company_id, supplier_id)
        REFERENCES suppliers(company_id, id)

This prevents a row owned by Company A from referencing a row owned by
Company B.

UUID uniqueness is not treated as an authorization mechanism.

---

## 9. Tenant-Aware Unique Constraints

Business identifiers that only need to be unique inside one company must
use tenant-aware uniqueness.

Example:

    UNIQUE (company_id, sku)

instead of:

    UNIQUE (sku)

unless the value is intentionally platform-global.

---

## 10. Tenant-Aware Indexes

Tenant-owned queries will frequently begin with company_id.

Indexes must therefore be designed around tenant access patterns.

Examples:

    (company_id, id)

    (company_id, supplier_id)

    (company_id, created_at)

    (company_id, status)

Redundant indexes should be avoided.

For example, if a B-tree index already exists on:

    (company_id, id)

a separate index only on:

    (company_id)

may be unnecessary because PostgreSQL can use the leftmost prefix of the
composite index.

Foreign keys do not automatically create indexes on referencing columns
in PostgreSQL.

Referencing-side indexes must therefore be evaluated explicitly.

---

## 11. Database Role Separation

The application uses separate database roles.

### ihrapanel_migrator

Responsible for:

- Flyway migrations
- schema creation
- table creation
- constraints
- indexes
- RLS configuration
- policies

### ihrapanel_app

Responsible for normal runtime application operations.

It may perform required DML operations on application tables:

- SELECT
- INSERT
- UPDATE
- DELETE

It must not be able to manage database structure or bypass tenant
security.

The runtime role must not have:

- SUPERUSER
- CREATEDB
- CREATEROLE
- BYPASSRLS

The runtime role must not own tenant tables.

---

## 12. Flyway

Schema changes are performed using Flyway.

Hibernate is configured with:

    spring.jpa.hibernate.ddl-auto=validate

Hibernate validates the schema but does not create or modify the
production schema.

Flyway connects using the migration role.

The application datasource connects using the runtime role.

The runtime role must not be allowed to modify:

    flyway_schema_history

This prevents the application from manipulating migration state.

---

## 13. Platform-Owned Tables

Not every table containing company-related information is automatically
tenant-owned.

Current platform-owned tables include:

- companies
- users
- exchange_rates

The users table contains company_id because each user belongs to one
company.

However, users remains a platform-level authentication table because
login and JWT validation must be able to locate users before tenant
database context is established.

Therefore RLS must not be blindly applied to all tables containing
company_id.

Tenant ownership must be explicitly decided for every new table.

---

## 14. Company Status

A valid user account is not sufficient for authentication.

Both conditions must hold:

    user.active = true
    company.active = true

If a company is inactive, its users must not receive authenticated access
even when:

- the user itself is active
- the JWT is cryptographically valid
- the JWT has not expired

This provides a company-level suspension mechanism.

---

## 15. Security Configuration

Public endpoints are intentionally limited.

Current public authentication-related endpoints include:

- /api/auth/register
- /api/auth/login

The health/development endpoint `/hello` is currently also public.

Company management endpoints are not public.

All other endpoints require authentication unless explicitly configured
otherwise.

Role-based authorization is applied in addition to tenant isolation.

Tenant isolation and role authorization solve different problems:

- RLS determines which company's rows may be accessed.
- Roles determine what an authenticated user may do.

Both are required.

---

## 16. Security Tests

The multi-tenant foundation is protected by automated tests.

The test suite covers:

### RLS CRUD

- same-tenant SELECT
- cross-tenant SELECT prevention
- same-tenant INSERT
- cross-tenant INSERT prevention
- same-tenant UPDATE
- cross-tenant UPDATE prevention
- same-tenant DELETE
- cross-tenant DELETE prevention

### Cross-Tenant Attacks

Tests include:

- accessing another tenant's row using a known UUID
- inserting a row with a forged company_id
- creating a relationship to another tenant's parent row
- executing tenant operations without TenantContext
- bypassing the service and accessing repositories directly
- direct repository access while another tenant is active

### Context Leakage

Tests verify:

- transaction-local tenant setting disappears after transaction completion
- Company A database context does not leak into Company B
- ThreadLocal context is cleared
- context is cleared even when request processing throws an exception

### Runtime Database Security

Tests verify that the application connects as:

    ihrapanel_app

and that the runtime role:

- is not SUPERUSER
- cannot create databases
- cannot create roles
- cannot bypass RLS
- cannot ALTER application tables
- cannot disable RLS
- cannot modify Flyway migration history

A full regression suite must pass before the tenant foundation is
considered stable.

---

## 17. Rules for Future Tenant-Owned Tables

Every new tenant-owned table introduced in future phases must be reviewed
against this checklist:

1. Does the table have `company_id UUID NOT NULL`?
2. Does it reference `companies(id)` where appropriate?
3. Is RLS enabled?
4. Is FORCE ROW LEVEL SECURITY enabled?
5. Does the RLS policy use `app.current_company_id`?
6. Are USING and WITH CHECK correctly defined?
7. Are relationships tenant-aware?
8. Are business unique constraints tenant-aware?
9. Are indexes designed with company_id in mind?
10. Does the service execute inside a transaction?
11. Does it call TenantDatabaseContext before tenant repository access?
12. Are same-tenant operations tested?
13. Are cross-tenant attacks tested?
14. Does the runtime role remain unable to modify the schema?

A feature is not considered complete until its tenant isolation behavior
is tested.

---

## 18. Consequences

### Positive

This architecture provides:

- one PostgreSQL database
- one migration path
- simpler tenant onboarding
- simpler backups
- simpler operations
- database-level tenant isolation
- defense in depth
- scalable tenant-aware querying
- centralized schema evolution

### Negative

Developers must consistently understand:

- tenant-owned vs platform-owned data
- transaction boundaries
- TenantContext lifecycle
- RLS policies
- tenant-aware constraints
- tenant-aware indexes

RLS configuration mistakes can be security-critical.

Therefore automated integration and attack tests are mandatory.

---

## 19. Alternatives Considered

### Database per Tenant

Advantages:

- strong physical/logical separation
- easier dedicated backup/restore per customer
- potentially useful for large enterprise customers

Disadvantages for the current project:

- many databases to provision
- migration orchestration across databases
- more connection pools
- more operational complexity
- more complicated monitoring and backups

Decision:

Not selected for the initial architecture.

It may be reconsidered later for dedicated enterprise tenants.

### Application-Level Filtering Only

Example:

    WHERE company_id = ?

Decision:

Rejected as the sole security mechanism.

Developer mistakes could accidentally omit the filter and expose
cross-tenant data.

Application filtering may still be used, but PostgreSQL RLS remains the
database security boundary.

---

## 20. Future Considerations

The architecture may evolve toward a hybrid model.

For example:

    Standard tenants
        ↓
    Shared PostgreSQL + RLS

    Large enterprise tenant
        ↓
    Dedicated PostgreSQL database

This would allow dedicated infrastructure without forcing every tenant
into database-per-tenant architecture.

ThreadLocal TenantContext is appropriate for the current synchronous
Spring MVC architecture.

If the application later adopts asynchronous or reactive request
processing, tenant context propagation must be redesigned accordingly.

---

## 21. Final Decision

For the current application architecture:

    Shared PostgreSQL
        +
    company_id
        +
    JWT-derived TenantContext
        +
    transaction-local PostgreSQL tenant context
        +
    PostgreSQL RLS
        +
    tenant-aware constraints
        +
    tenant-aware indexes
        +
    runtime/migration role separation
        +
    automated cross-tenant security tests

is the accepted multi-tenant architecture.

All future tenant-owned modules must follow this contract.
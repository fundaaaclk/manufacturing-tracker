# Multi-Tenancy Architecture

## Architecture

The application uses a shared PostgreSQL database with tenant isolation.

Tenant identity is represented by `company_id`.

Tenant isolation is enforced using:

- JWT tenant identity
- Application TenantContext
- PostgreSQL Row Level Security (RLS)
- Tenant-aware foreign keys
- Tenant-aware unique constraints
- Tenant-aware indexes
- Cross-tenant integration tests

---

## Platform-Owned Tables

Platform-owned tables are not tenant-isolated using operational RLS.

Current platform tables:

- companies
- users
- exchange_rates

A platform table may contain a `company_id` reference without being
considered tenant-owned.

Example: `users.company_id`.

Authentication must be able to locate a user globally by email before
TenantContext has been established.

---

## Tenant-Owned Tables

Every tenant-owned operational table MUST contain:

company_id UUID NOT NULL

Examples include:

- products
- warehouses
- stock_movements
- suppliers
- supplier_transactions
- purchases
- purchase_items
- production_orders
- production_costs
- checks
- vat_records
- exports
- receivables
- ledger_entries

Every tenant-owned row belongs to exactly one company.

---

## Tenant Relationships

Relationships between tenant-owned tables MUST preserve tenant ownership.

Example:

Supplier:

UNIQUE (company_id, id)

Purchase -> Supplier:

FOREIGN KEY (company_id, supplier_id)
REFERENCES suppliers(company_id, id)

A row owned by Company A must never reference a tenant-owned row
belonging to Company B.

---

## Tenant-Aware Uniqueness

Business identifiers that only need to be unique inside a company
MUST include company_id.

Example:

UNIQUE (company_id, sku)

instead of:

UNIQUE (sku)

---

## Row Level Security

Tenant-owned tables MUST use PostgreSQL Row Level Security.

Required configuration:

ENABLE ROW LEVEL SECURITY
FORCE ROW LEVEL SECURITY

RLS policies must restrict both reading and writing using company_id.

The runtime database role must:

- not own tenant tables
- not have BYPASSRLS
- not be a superuser

Missing tenant context must fail closed.

---

## Security Principle

Application-level filtering is not considered sufficient tenant isolation.

Tenant isolation must remain effective even if application code
accidentally executes an unfiltered query.
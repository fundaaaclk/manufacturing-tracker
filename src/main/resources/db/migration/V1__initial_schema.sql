CREATE TABLE companies (
    id UUID PRIMARY KEY,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    name VARCHAR(255) NOT NULL,
    tax_number VARCHAR(255),

    CONSTRAINT uk_companies_tax_number
        UNIQUE (tax_number)
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    company_id UUID NOT NULL,

    CONSTRAINT uk_users_email
        UNIQUE (email),

    CONSTRAINT chk_users_role
        CHECK (role IN ('OWNER', 'ACCOUNTANT', 'WAREHOUSE')),

    CONSTRAINT fk_users_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id)
);

CREATE TABLE exchange_rates (
    id UUID PRIMARY KEY,
    base_currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    effective_at TIMESTAMPTZ NOT NULL,
    quote_currency VARCHAR(3) NOT NULL,
    rate NUMERIC(19,6) NOT NULL,
    source VARCHAR(20) NOT NULL,

    CONSTRAINT chk_exchange_rates_base_currency
        CHECK (base_currency IN ('TRY', 'EUR', 'USD')),

    CONSTRAINT chk_exchange_rates_quote_currency
        CHECK (quote_currency IN ('TRY', 'EUR', 'USD')),

    CONSTRAINT chk_exchange_rates_source
        CHECK (source IN ('TCMB', 'MANUAL')),

    CONSTRAINT uk_exchange_rate_pair_time_source
        UNIQUE (
            base_currency,
            quote_currency,
            effective_at,
            source
        )
);

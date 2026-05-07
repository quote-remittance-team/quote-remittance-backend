CREATE TABLE users (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE quotes (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    exchange_rate NUMERIC(18,16) NOT NULL,
    expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    fee NUMERIC(18,2) NOT NULL,
    from_currency VARCHAR(3) NOT NULL,
    quote_reference VARCHAR(255) NOT NULL,
    receive_amount NUMERIC(18,2) NOT NULL,
    send_amount NUMERIC(18,2) NOT NULL,
    status VARCHAR(255) NOT NULL CHECK ( status IN ('ACTIVE', 'USED', 'EXPIRED')),
    to_currency VARCHAR(3) NOT NULL,
    total_payable NUMERIC(18,2) NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT uk_quote_reference UNIQUE (quote_reference),
    CONSTRAINT fk_quotes_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE TABLE deposits (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(255) NOT NULL CHECK ( status IN ('PENDING', 'CONFIRMED', 'FAILED')),
    payment_reference VARCHAR(255) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    quote_id UUID NOT NULL,
    CONSTRAINT uk_deposits_idempotency UNIQUE (idempotency_key),
    CONSTRAINT fk_deposits_quote_id FOREIGN KEY (quote_id) REFERENCES quotes(id)
);
CREATE TABLE remittances (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    exchange_rate NUMERIC(18,16) NOT NULL,
    idempotency_key VARCHAR(255),
    receive_amount NUMERIC(18,2) NOT NULL,
    receiver_account_number VARCHAR(255) NOT NULL,
    receiver_bank_code VARCHAR(255) NOT NULL,
    receiver_name VARCHAR(255) NOT NULL,
    reference VARCHAR(255) NOT NULL,
    send_amount NUMERIC(18,2) NOT NULL,
    status VARCHAR(255) NOT NULL CHECK ( status IN ('PROCESSING', 'COMPLETED', 'FAILED')),
    deposit_id UUID NOT NULL,
    quote_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    CONSTRAINT uk_remittances_reference UNIQUE (reference),
    CONSTRAINT uk_remittances_deposit UNIQUE (deposit_id),
    CONSTRAINT uk_remittances_idempotency UNIQUE (idempotency_key),
    CONSTRAINT fk_remittances_deposit_id FOREIGN KEY (deposit_id) REFERENCES deposits(id),
    CONSTRAINT fk_remittances_quotes_id FOREIGN KEY (quote_id) REFERENCES quotes(id),
    CONSTRAINT fk_remittances_sender_id FOREIGN KEY (sender_id) REFERENCES users(id)
);
CREATE TABLE payouts (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    provider_reference VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL CHECK ( status IN  ('PENDING', 'COMPLETED', 'FAILED')),
    remittance_id UUID NOT NULL,
    CONSTRAINT uk_payout_provider_reference UNIQUE (provider_reference),
    CONSTRAINT uk_payout_remittance UNIQUE (remittance_id),
    CONSTRAINT fk_payouts_remittance_id FOREIGN KEY (remittance_id) REFERENCES remittances(id)
);
CREATE TABLE  notifications (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    message VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL CHECK ( status IN ('PENDING', 'SENT', 'FAILED')),
    user_id UUID NOT NULL,
    CONSTRAINT fk_notifications_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);
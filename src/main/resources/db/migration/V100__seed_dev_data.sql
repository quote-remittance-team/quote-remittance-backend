INSERT INTO users (id, created_at, updated_at, email, password_hash)
VALUES (
        'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        'test@remitance.com',
        '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HCGKKwOqX.yrz.lM0u4M6'
       );

INSERT INTO quotes (id, created_at, updated_at, exchange_rate, expires_at, fee, from_currency, quote_reference, receive_amount, send_amount, status, to_currency, total_payable, user_id)
VALUES (
        'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        0.0007100000000000,
        CURRENT_TIMESTAMP + INTERVAL '5 minutes',
        50.00,
        'NGN',
        'QR-987654321',
        35.50,
        50000.00,
        'ACTIVE',
        'USD',
        50500.00,
        'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d'
       );

INSERT INTO deposits (id, created_at, updated_at, amount, currency, status, payment_reference, idempotency_key, quote_id)
VALUES (
           'c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP,
           50500.00,
           'NGN',
           'CONFIRMED',
           'PAY-1122334455',
           'idem-dep-test-001',
           'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e'
       );
INSERT INTO remittances (id, created_at, updated_at, exchange_rate, idempotency_key, receive_amount, receiver_account_number, receiver_bank_code, receiver_name, reference, send_amount, status, deposit_id, quote_id, sender_id)
VALUES (
           'd4e5f6a7-b89c-0d1e-2f3a-4b5c6d7e8f9a',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP,
           0.0007100000000000,
           'idem-remit-test-001',
           35.50,
           '0123456789',
           '044',
           'John Doe Receiver',
           'REM-1122334455',
           50000.00,
           'PROCESSING',
           'c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f',
           'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e',
           'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d'
       );

INSERT INTO payouts (id, created_at, updated_at, provider_reference, status, remittance_id)
VALUES (
           'e5f6a7b8-9c0d-1e2f-3a4b-5c6d7e8f9a0b',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP,
           'PROV-OUT-998877',
           'PENDING',
           'd4e5f6a7-b89c-0d1e-2f3a-4b5c6d7e8f9a'
       );

INSERT INTO notifications (id, created_at, updated_at, message, status, user_id)
VALUES (
           'f6a7b89c-0d1e-2f3a-4b5c-6d7e8f9a0b1c',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP,
           'Your remittance of 50,000 NGN is currently processing.',
           'SENT',
           'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d'
       );
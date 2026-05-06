INSERT INTO users (id, created_at, updated_at, email, password_hash)
VALUES (
        '123e4567-e89d-12d3-a456-426614174000',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        'de-test@oldtrafford.com',
        '$2a$12$uCkkXmhW5ThVK8mpBvnXOOJRLd64LJeHTeCkSuB3lfaR2NOAYBaSi'
       ) ON CONFLICT (email) DO NOTHING;

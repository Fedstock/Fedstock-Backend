ALTER TABLE users
    ADD COLUMN IF NOT EXISTS username VARCHAR(255);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS store_id VARCHAR(100);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS role VARCHAR(30) DEFAULT 'USER';

UPDATE users
SET username = email
WHERE username IS NULL;

UPDATE users
SET store_id = 'legacy-' || id
WHERE store_id IS NULL;

UPDATE users
SET role = 'USER'
WHERE role IS NULL;

ALTER TABLE users
    ALTER COLUMN username SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN store_id SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN role SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_username
    ON users (username);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_store_id
    ON users (store_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_users_role'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'));
    END IF;
END $$;

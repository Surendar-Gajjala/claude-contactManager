-- ContactManagerApp database schema (cmsPrompt.txt sections 28-32)
--
-- Fresh setup:
--   psql -U postgres -h localhost -c "CREATE DATABASE contact_manager;"
--   psql -U postgres -h localhost -d contact_manager -f db/schema.sql

CREATE TABLE IF NOT EXISTS persons (
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    gender      VARCHAR(20)  NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    address     VARCHAR(255),
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS contacts (
    id            BIGSERIAL PRIMARY KEY,
    person_id     BIGINT NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
    phone_number  VARCHAR(20) NOT NULL,
    contact_type  VARCHAR(20) NOT NULL CHECK (contact_type IN ('PERSONAL', 'HOME', 'WORK', 'OTHER')),
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

-- Minimum index set (cmsPrompt.txt section 32) — email/PKs are already indexed via
-- their UNIQUE/PRIMARY KEY constraints above, so they're not repeated here.
CREATE INDEX IF NOT EXISTS idx_persons_first_name   ON persons (first_name);
CREATE INDEX IF NOT EXISTS idx_persons_last_name    ON persons (last_name);
CREATE INDEX IF NOT EXISTS idx_contacts_person_id   ON contacts (person_id);
CREATE INDEX IF NOT EXISTS idx_contacts_phone_number ON contacts (phone_number);
CREATE INDEX IF NOT EXISTS idx_contacts_contact_type ON contacts (contact_type);

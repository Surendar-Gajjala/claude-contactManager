-- One-time corrective migration for a `contact_manager` database that was created by
-- some other prior process with a schema that doesn't match cmsPrompt.txt sections 29-32:
--   * persons was missing the `address` column
--   * contacts.contact_type CHECK allowed MOBILE instead of PERSONAL
--   * timestamp columns had no DB-level DEFAULT now()
--   * required indexes on first_name/last_name/phone_number/contact_type were missing
-- Safe to run only while both tables are empty (verified before running).

ALTER TABLE persons ADD COLUMN IF NOT EXISTS address VARCHAR(255);

ALTER TABLE persons  ALTER COLUMN created_at SET DEFAULT now();
ALTER TABLE persons  ALTER COLUMN updated_at SET DEFAULT now();
ALTER TABLE contacts ALTER COLUMN created_at SET DEFAULT now();
ALTER TABLE contacts ALTER COLUMN updated_at SET DEFAULT now();

ALTER TABLE contacts DROP CONSTRAINT IF EXISTS contacts_contact_type_check;
ALTER TABLE contacts ADD CONSTRAINT contacts_contact_type_check
    CHECK (contact_type IN ('PERSONAL', 'HOME', 'WORK', 'OTHER'));

CREATE INDEX IF NOT EXISTS idx_persons_first_name    ON persons (first_name);
CREATE INDEX IF NOT EXISTS idx_persons_last_name     ON persons (last_name);
CREATE INDEX IF NOT EXISTS idx_contacts_phone_number ON contacts (phone_number);
CREATE INDEX IF NOT EXISTS idx_contacts_contact_type ON contacts (contact_type);
-- contacts.person_id is already the leading column of the existing composite unique
-- index uk_contacts_person_phone (person_id, phone_number), so a dedicated index would
-- be redundant.

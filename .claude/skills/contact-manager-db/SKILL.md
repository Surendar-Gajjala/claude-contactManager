---
name: contact-manager-db
description: Use when creating, modifying, or reviewing the ContactManagerApp PostgreSQL schema — the persons/contacts tables, constraints, foreign keys, cascade-delete behavior, or indexes.
---

# ContactManagerApp — Database

Full spec: [cmsPrompt.txt](../../../cmsPrompt.txt) sections 28–32. Part of the
`contact-manager-app` skill family — see that skill for the overall build order and
cross-cutting rules; this one covers Step 1 (Database) in detail.

## Dev connection

```
URL:      jdbc:postgresql://localhost:5432/contact_manager
Username: postgres
Password: postgres
```

Never hard-code these for production — use environment variables / external config there.

## Step-by-step build process

1. Create the `contact_manager` PostgreSQL database.
2. Create the `persons` table (columns below), with `email` UNIQUE and `gender` constrained to
   the enum values.
3. Create the `contacts` table (columns below), with `person_id` as a `FOREIGN KEY … REFERENCES
   persons(id) ON DELETE CASCADE` and `contact_type` constrained to the enum values.
4. Add indexes (list below) — no others; don't index speculatively.
5. Verify: insert a person + two contacts, delete the person, confirm both contacts are gone
   automatically (cascade, not application-level cleanup).

## Schema reference

**persons**

| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL / UUID | PRIMARY KEY |
| first_name | VARCHAR(100) | NOT NULL |
| last_name | VARCHAR(100) | NOT NULL |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| gender | VARCHAR(20) | NOT NULL, CHECK IN (MALE, FEMALE, OTHER) |
| address | VARCHAR(255) | NULL |
| created_at | TIMESTAMP | NOT NULL DEFAULT now() |
| updated_at | TIMESTAMP | NOT NULL DEFAULT now() |

**contacts**

| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL / UUID | PRIMARY KEY |
| person_id | BIGINT / UUID | NOT NULL, FOREIGN KEY → persons.id, ON DELETE CASCADE |
| phone_number | VARCHAR(20) | NOT NULL |
| contact_type | VARCHAR(20) | NOT NULL, CHECK IN (PERSONAL, HOME, WORK, OTHER) |
| created_at | TIMESTAMP | NOT NULL DEFAULT now() |
| updated_at | TIMESTAMP | NOT NULL DEFAULT now() |

`contact_type` values must match the backend `ContactType` enum exactly.

## Indexes (minimum set, no more)

- `persons.first_name`
- `persons.last_name`
- `persons.email`
- `contacts.person_id`
- `contacts.phone_number`
- `contacts.contact_type`

## Verification

Before considering the database step done, confirm:
- PostgreSQL is running and `contact_manager` is accessible.
- Both tables, their constraints, and the FK cascade exist as specified.
- The JPA `@OneToMany`/`@ManyToOne` mapping on the entities (built in the backend skill) is
  configured consistent with `ON DELETE CASCADE` — don't let the DB and JPA delete behavior
  diverge.

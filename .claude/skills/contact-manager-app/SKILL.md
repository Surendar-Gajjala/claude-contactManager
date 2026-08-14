---
name: contact-manager-app
description: Use when starting, planning, or reviewing any work in ContactManagerApp (Spring Boot + PostgreSQL backend, React + TS frontend for Person/Contact management with Excel import) — the entry point for the project's architecture rules, build order, and final verification gate.
---

# ContactManagerApp

Full spec: [cmsPrompt.txt](../../../cmsPrompt.txt) at the project root. This is the entry-point
skill — read it first, then jump to the layer-specific skill for the work at hand:

- **REQUIRED SUB-SKILL:** `contact-manager-db` — database schema, constraints, cascade delete.
- **REQUIRED SUB-SKILL:** `contact-manager-backend` — entities, services, controllers, REST
  APIs, Excel import, backend build/test commands.
- **REQUIRED SUB-SKILL:** `contact-manager-frontend` — pages, forms, API layer, frontend
  build/lint commands.

## Non-negotiable architecture rules (apply everywhere)

- Layered architecture only: Controller → Service → Repository → PostgreSQL. No microservices.
- No authentication/authorization unless the user explicitly asks for it.
- No over-engineering: no speculative abstractions, no features outside `cmsPrompt.txt`.
- DTOs only cross the API boundary — never expose JPA entities from a controller.
- One global `@RestControllerAdvice` handles all backend errors; never leak stack traces.
- Frontend pages never call Axios directly — always through `src/api/*Api.ts`.

## Build order (high level — see sub-skills for the step-by-step detail)

- **Step 1** — Database (`contact-manager-db`)
- **Steps 2–12** — Backend: setup, entities, repositories, DTOs, services, controllers,
  exception handling, Person CRUD, Contact CRUD, Excel import, backend testing
  (`contact-manager-backend`)
- **Steps 13–19** — Frontend: setup, layout, Persons UI, Contacts UI, form validation, API
  integration, final UI testing (`contact-manager-frontend`)

Follow this order top to bottom — each step assumes the previous one compiles/passes. Do not
jump ahead to frontend work while backend steps are incomplete, or vice versa.

## Process rules for every change

1. Inspect the existing project structure before adding anything; reuse what's there.
2. Don't overwrite working code unnecessarily.
3. Implement exactly one feature at a time.
4. After a backend change: compile and run backend tests (`contact-manager-backend` has the
   commands).
5. After a frontend change: run type-check/lint (`contact-manager-frontend` has the commands).
6. Fix all errors before moving to the next feature.
7. Never implement anything outside `cmsPrompt.txt`.

## Other established conventions (undocumented in spec)

- An active `PostToolUse` hook (`.claude/settings.local.json`, personal/gitignored) auto-formats
  saved files: `google-java-format` for `*.java`, `prettier` for
  `*.ts/tsx/js/jsx/json/css/scss/md`. Don't hand-format against it or fight its output.
- `db/fix_existing_schema.sql` is a one-off migration/patch script, not part of the initial
  `schema.sql` build — a fresh setup doesn't need to run it.

## Final full-stack verification gate

Do not call ContactManagerApp complete until **both** succeed:

```bash
cd backend  && mvn clean test && mvn clean package
cd frontend && npm install && npm run lint && npm run typecheck && npm run build
```

Then confirm end-to-end: PostgreSQL running and `contact_manager` reachable; backend and
frontend both start; frontend talks to the backend; Person CRUD, Contact CRUD, search,
pagination, sorting, Excel upload (incl. positional phone/contact-type mapping), and delete
confirmation (incl. cascade to contacts) all work. A failing frontend or backend build means the
project is not complete, regardless of what else works.

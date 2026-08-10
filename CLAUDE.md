# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

Greenfield — only planning artifacts exist right now: the spec (`cmsPrompt.txt`), this file,
`README.md`, and the skill family under `.claude/skills/`. There is no `backend/` or `frontend/`
source tree yet.

## Where to look first

- Full spec: [cmsPrompt.txt](./cmsPrompt.txt) — the source of truth for every rule, field,
  endpoint, and command below.
- Skill family under `.claude/skills/` — invoke `contact-manager-app` first (entry point:
  architecture rules, build order, final verification gate), then whichever of
  `contact-manager-db`, `contact-manager-backend`, or `contact-manager-frontend` matches the
  layer you're touching. Each skill has the full step-by-step build process and command
  reference for its layer.

## Architecture

Simple layered architecture, no microservices, no auth/authz unless explicitly requested:

```
Controller → Service → Repository → PostgreSQL
```

- **Backend** (`backend/`, planned) — Java 17 / Spring Boot 3, Maven. Controllers stay thin
  (`@Valid` the DTO, call one service method, return the response — never touch a JPA entity).
  Business rules, transactions, and Excel-import logic live in services. Repositories only
  persist/query. One `@RestControllerAdvice` (`GlobalExceptionHandler`) handles all errors.
- **Frontend** (`frontend/`, planned) — React + TypeScript + Vite. Pages never call Axios
  directly — always through `src/api/*Api.ts` (`axiosClient.ts`, `personApi.ts`,
  `contactApi.ts`).
- **Domain** — one `Person` has many `Contact`s (`ON DELETE CASCADE` at the DB level, mirrored
  in the JPA mapping). DTOs (`PersonCreateRequest`/`UpdateRequest`/`Response`,
  `ContactCreateRequest`/`UpdateRequest`/`Response`) are the only objects that cross the API
  boundary — never expose JPA entities from a controller.

## Commands

### Database
```
jdbc:postgresql://localhost:5432/contact_manager   (postgres / postgres, dev only)
```

### Backend (once scaffolded, run from `backend/`)
```bash
mvnw.cmd spring-boot:run           # dev server (Windows); ./mvnw spring-boot:run on Linux/macOS
mvn clean compile                   # compile
mvn test                            # run all tests
mvn test -Dtest=ClassName#method    # run a single test
mvn clean test                      # clean + full test run
mvn clean package                   # build the production JAR (never swap -DskipTests in for a real test run)
java -jar target/<app-name>.jar     # run the built JAR
```

### Frontend (once scaffolded, run from `frontend/`)
```bash
npm install
npm run dev           # http://localhost:5173 (or the port Vite reports)
npm run lint
npm run typecheck     # or the project's configured TS validation command if this isn't defined
npm run build          # production build — must succeed with no TS/compile errors
```

### Definition of done
```bash
cd backend  && mvn clean test && mvn clean package
cd frontend && npm install && npm run lint && npm run typecheck && npm run build
```
A failing frontend or backend build means the project is not complete, regardless of what else
works.

## Binding rules

- No authentication or authorization unless explicitly requested.
- No over-engineering — no abstractions or features beyond `cmsPrompt.txt`.
- Implement one feature at a time, following the build order in the skill files.
- After a backend change: compile and run backend tests. After a frontend change: run
  lint/typecheck. Fix errors before moving to the next feature.
- Reuse existing code where appropriate; don't overwrite working code unnecessarily.

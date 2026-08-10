# ContactManagerApp

A simple full-stack Contact Management application for managing **Persons** and their
**Contacts** (phone numbers), with search, pagination, sorting, form validation, and bulk
creation via Excel upload.

> **Status:** greenfield — this repository currently contains only planning artifacts
> (`cmsPrompt.txt`, this README, `CLAUDE.md`, and the `contact-manager-app` skill family under
> `.claude/skills/`). No backend or frontend code has been written yet. Sections below describe
> the target architecture and the workflow once the project is scaffolded.

The full specification lives in [cmsPrompt.txt](./cmsPrompt.txt) — treat it as the source of
truth; this README summarizes it.

## Features

- Person CRUD (create, read, update, delete)
- Contact CRUD, each Contact belongs to one Person
- Search Persons/Contacts by person name
- Pagination and sorting on both list views
- Client-side form validation
- Bulk Person + Contact creation via Excel upload, with positional phone-number ↔
  contact-type mapping
- Deleting a Person cascades to delete their Contacts

## Tech stack

### Frontend
| | |
|---|---|
| Framework | React + TypeScript |
| Build tool | Vite |
| Routing | React Router |
| HTTP client | Axios |
| Forms | React Hook Form + Zod |
| Styling | Tailwind CSS / Tailwind UI |

### Backend
| | |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x (Spring Web, Spring Data JPA) |
| ORM | Hibernate |
| Validation | Jakarta Bean Validation |
| Boilerplate | Lombok |
| Build tool | Maven |
| Database | PostgreSQL |
| Testing | JUnit 5, Mockito, Spring Boot Test |

## Architecture

Simple layered architecture, no microservices, no auth/authz unless explicitly requested:

```
Controller  →  Service  →  Repository  →  PostgreSQL
```

- **Controller** — receives requests, validates DTOs (`@Valid`), delegates to a service, returns
  a response. Never touches JPA entities directly.
- **Service** — business rules, transactions, Person↔Contact relationship logic, Excel import.
- **Repository** — persistence, pagination, sorting, search queries.

DTOs (`PersonCreateRequest`, `PersonUpdateRequest`, `PersonResponse`, `ContactCreateRequest`,
`ContactUpdateRequest`, `ContactResponse`) are the only objects that cross the API boundary.
A single `@RestControllerAdvice` (`GlobalExceptionHandler`) handles all error responses.

## Planned project structure

```
ContactManagerApp/
├── backend/                # Spring Boot app (Maven)
│   └── src/main/java/...   # controller / service / repository / dto / entity / exception
├── frontend/                # React + TS + Vite app
│   └── src/
│       ├── api/             # axiosClient.ts, personApi.ts, contactApi.ts
│       └── pages/           # Persons, Contacts pages
├── cmsPrompt.txt            # full specification
├── CLAUDE.md                # durable project instructions for Claude Code
└── .claude/skills/
    ├── contact-manager-app/       # entry point: architecture rules, build order, final gate
    ├── contact-manager-db/        # schema, constraints, cascade delete
    ├── contact-manager-backend/   # entities, services, controllers, APIs, Excel import
    └── contact-manager-frontend/  # pages, forms, API layer
```

## REST API summary

### Persons
| Method | Path | Notes |
|---|---|---|
| POST | `/api/persons` | Create a person |
| GET | `/api/persons?page=0&size=6&sort=firstName,asc&search=Surendar` | Paginated, sortable, searchable by name |
| GET | `/api/persons/{id}` | Get one |
| PUT | `/api/persons/{id}` | Update |
| DELETE | `/api/persons/{id}` | Delete (cascades to contacts) |

### Contacts
| Method | Path | Notes |
|---|---|---|
| POST | `/api/contacts` | Body: `{ "personId": 1, "phoneNumber": "9618443676", "contactType": "PERSONAL" }` |
| GET | `/api/contacts?page=0&size=6&sort=phoneNumber,asc&search=Surendar` | Paginated, sortable, searchable by person name |
| GET | `/api/contacts/{contactId}` | Get one |
| PUT | `/api/contacts/{contactId}` | Update |
| DELETE | `/api/contacts/{contactId}` | Delete |

### Excel import
`POST /api/persons/upload` — multipart `file` field. See below.

## Excel bulk import

Columns: `FirstName, LastName, Email, Gender, Address, PhoneNumbers, ContactType`.

`PhoneNumbers` and `ContactType` are comma-separated and mapped **positionally**:

```
PhoneNumbers: 9618443676, 7893097820
ContactType:  HOME,       PERSONAL
```

produces one Person plus two Contacts: `(9618443676, HOME)` and `(7893097820, PERSONAL)`.
The number of phone numbers must exactly match the number of contact types per row, or that
row is rejected. The whole upload is processed in a single transaction — any failure rolls back
the entire import.

## Getting started (once scaffolded)

### Database
```sql
-- PostgreSQL, dev credentials (do not use in production)
-- URL:      jdbc:postgresql://localhost:5432/contact_manager
-- Username: postgres
-- Password: postgres
```

### Backend
```bash
cd backend
mvnw.cmd spring-boot:run   # Windows
./mvnw spring-boot:run     # Linux/macOS
```

### Frontend
```bash
cd frontend
npm install
npm run dev   # http://localhost:5173 (or the port Vite reports)
```

## Build, lint, and test commands

### Backend
```bash
cd backend
mvn clean compile   # compile
mvn clean test       # run tests
mvn clean package    # build the production JAR (tests must pass — don't skip with -DskipTests)
java -jar target/<application-name>.jar   # run the built JAR
```

### Frontend
```bash
cd frontend
npm run lint         # fix all errors before considering the frontend done
npm run typecheck    # or the project's configured TS validation command
npm run build         # production build — must succeed with no TS/compile errors
npm run preview        # preview the production build
```

## Full-stack verification

Before calling ContactManagerApp complete, both of these must succeed:

```bash
# Backend
cd backend && mvn clean test && mvn clean package

# Frontend
cd frontend && npm install && npm run lint && npm run typecheck && npm run build
```

Then confirm end-to-end:

1. PostgreSQL is running and `contact_manager` is accessible.
2. Backend starts and its APIs are reachable.
3. Frontend starts and talks to the backend.
4. Person CRUD, Contact CRUD, search, pagination, and sorting all work.
5. Excel upload works, including positional phone-number/contact-type mapping.
6. Delete confirmation works, and deleting a Person removes its Contacts.
7. Backend tests pass and the production JAR builds.
8. The frontend production build succeeds.

A failing frontend or backend build means the project is **not** complete, regardless of what
else works.

## Development workflow

See [CLAUDE.md](./CLAUDE.md) and the skill family under `.claude/skills/` —
`contact-manager-app` (entry point / architecture / build order / final gate),
`contact-manager-db`, `contact-manager-backend`, and `contact-manager-frontend` — for the
required build order and process rules (one feature at a time, compile/test after backend
changes, type-check/lint after frontend changes, etc.).

---
name: contact-manager-backend
description: Use when implementing, extending, testing, compiling, or running the ContactManagerApp Spring Boot backend — entities, DTOs, services, controllers, exception handling, REST APIs, or the Excel import feature.
---

# ContactManagerApp — Backend

Full spec: [cmsPrompt.txt](../../../cmsPrompt.txt) sections 14–27, 33, 37. Part of the
`contact-manager-app` skill family — see that skill for cross-cutting rules shared with the
frontend; this one covers Steps 2–12 of the build order and the backend run/test/build commands.

## Non-negotiable rules

- Layered: Controller → Service → Repository → PostgreSQL. No microservices, no unnecessary
  abstractions.
- No authentication/authorization unless explicitly requested.
- Controllers are thin: `@Valid` the DTO, call one service method, return the response. Never
  touch a JPA entity or return one directly.
- Services own business rules, transactions, Person↔Contact relationship logic, and Excel
  processing.
- Repositories only persist and query — pagination, sorting, name search, email-existence checks.
- One `@RestControllerAdvice` (`GlobalExceptionHandler`) for all errors. Never leak stack traces.

## Step-by-step build process (Steps 2–12)

1. **Setup** — Spring Boot 3.x project with Spring Web, Spring Data JPA, Hibernate, Jakarta Bean
   Validation, Lombok, Maven, PostgreSQL driver.
2. **Entities/enums** — `Person`, `Contact`, `Gender`, `ContactType` (see reference below);
   configure the `Person 1—Many Contact` relationship consistent with the DB's `ON DELETE
   CASCADE` (see `contact-manager-db` skill).
3. **Repositories** — `PersonRepository`, `ContactRepository` with pagination, sorting, name
   search, and an email-existence query.
4. **DTOs** — `PersonCreateRequest`, `PersonUpdateRequest`, `PersonResponse`,
   `ContactCreateRequest`, `ContactUpdateRequest`, `ContactResponse`. Bean Validation on every
   request DTO (see validation rules below).
5. **Services** — `PersonService`, `ContactService`, `ExcelImportService`. Email uniqueness is
   checked here first, with the DB UNIQUE constraint as the final safeguard.
6. **Controllers** — `PersonController`, `ContactController`, thin, `@Valid` on request params.
7. **Exception handling** — `GlobalExceptionHandler` covering: not found, duplicate email,
   validation errors, invalid request, DB constraint violations, Excel import errors, unexpected
   errors.
8. **Person CRUD** — implement + test all Person APIs.
9. **Contact CRUD** — implement + test all Contact APIs.
10. **Excel import** — `POST /api/persons/upload`, validation + positional mapping + transaction
    (see below).
11. **Backend testing** — run the full suite (`mvn clean test`), fix all failures before moving on.

## Validation rules

- Person: `firstName`/`lastName` — `@NotBlank`, `@Size(max = 100)`; `email` — `@NotBlank`,
  `@Email` (uniqueness enforced in service + DB); `gender` — `@NotNull`.
- Contact: `phoneNumber` — `@NotBlank` + pattern validation; `contactType` — `@NotNull`.

## Entity reference

**Person** — id, firstName, lastName, email (unique), gender (`MALE|FEMALE|OTHER`), address,
contacts (1→many), createdDate, updatedDate.

**Contact** — id, person (many→1), phoneNumber, contactType (`PERSONAL|HOME|WORK|OTHER`),
createdDate, updatedDate.

## REST API reference

| Method | Path | Notes |
|---|---|---|
| POST | `/api/persons` | Create |
| GET | `/api/persons?page=0&size=6&sort=firstName,asc&search=Surendar` | Paginated/sortable/searchable by name |
| GET | `/api/persons/{id}` | Get one |
| PUT | `/api/persons/{id}` | Update |
| DELETE | `/api/persons/{id}` | Delete (cascades to contacts) |
| POST | `/api/contacts` | Body: `{ "personId": 1, "phoneNumber": "9618443676", "contactType": "PERSONAL" }` |
| GET | `/api/contacts?page=0&size=6&sort=phoneNumber,asc&search=Surendar` | Paginated/sortable/searchable by person name |
| GET/PUT/DELETE | `/api/contacts/{contactId}` | Get/update/delete one |
| POST | `/api/persons/upload` | multipart `file` — Excel bulk import |

## Excel import rules

Columns: `FirstName, LastName, Email, Gender, Address, PhoneNumbers, ContactType`.
`PhoneNumbers` and `ContactType` are comma-separated, trimmed, and mapped **positionally**:
`PhoneNumber[i]` ↔ `ContactType[i]`. Counts must match exactly per row, or reject that row with a
clear error. The whole upload is one transaction: read → validate structure → validate all rows →
create Person records → create Contact records → commit; any failure rolls back everything —
never leave partial Person/Contact data behind.

## Testing requirements

Person: create, paginate, search by name, update, delete, duplicate-email rejection, not-found.
Contact: create, paginate, search by person name, update, delete, not-found, invalid phone number.
Excel: single-phone import, multi-phone import, multi-phone→multi-type mapping, count mismatch,
invalid email, duplicate email, invalid gender, invalid contact type, missing column, and
transaction rollback on failure.

## Run / build / test commands

```bash
cd backend

# dev server
mvnw.cmd spring-boot:run        # Windows
./mvnw spring-boot:run          # Linux/macOS
mvn spring-boot:run             # if no wrapper

mvn clean compile               # compile
mvn test                        # run tests
mvn clean test                  # clean + test
mvn clean package               # build the JAR (don't use -DskipTests in place of running tests)
java -jar target/<app-name>.jar # run the built JAR
```

**Backend verification** (must both succeed before calling the backend done):
```bash
mvn clean test
mvn clean package
```

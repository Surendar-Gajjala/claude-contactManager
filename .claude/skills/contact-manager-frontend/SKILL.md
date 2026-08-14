---
name: contact-manager-frontend
description: Use when implementing, extending, testing, linting, or building the ContactManagerApp React + TypeScript frontend — the Persons/Contacts pages, forms, API layer, validation, pagination, or Excel upload UI.
---

# ContactManagerApp — Frontend

Full spec: [cmsPrompt.txt](../../../cmsPrompt.txt) sections 2–13, 36. Part of the
`contact-manager-app` skill family — see that skill for cross-cutting rules shared with the
backend; this one covers Steps 13–19 of the build order and the frontend run/lint/build commands.

## Non-negotiable rules

- Stack: React + TypeScript + Vite, React Router, Axios, React Hook Form + Zod, Tailwind CSS.
- Left-side nav layout, title "ContactManagerApp", nav items: Persons, Contacts.
- Never call Axios directly from a page component — always go through `src/api/*Api.ts`.
- Every list view needs: loading, success, validation-error, API-error, empty-data, and
  delete-confirmation states. Excel upload needs progress/success/failure states too.

## Step-by-step build process (Steps 13–19)

1. **Setup** — scaffold the Vite React+TS app, install React Router, Axios, React Hook Form,
   Zod, Tailwind.
2. **Layout** — left-nav shell switching between the Persons and Contacts pages.
3. **Persons UI** — table (columns below), search bar (by person name), Add Person button,
   Excel upload icon/button, pagination footer, Edit/Delete icon actions. Delete asks for
   confirmation and warns that it also deletes the person's contacts.
4. **Contacts UI** — table (columns below), search bar (by person name), Add Contact button,
   pagination footer, Edit/Delete icon actions with confirmation.
5. **Form validation** — React Hook Form + Zod on the Person and Contact create/edit forms
   (rules below), messages shown beside each field.
6. **API integration** — wire every screen to the backend through the `src/api/` layer
   (`personApi.ts`, `contactApi.ts`, both built on a shared `axiosClient.ts`).
7. **Final pass** — verify CRUD, search, sort, pagination, validation, delete confirmation,
   Excel upload (including multi-contact rows), error handling, loading/empty states, and
   responsiveness.

## API layer

```
src/api/
  axiosClient.ts
  personApi.ts   → createPerson, getPersons, getPersonById, updatePerson, deletePerson, uploadExcel
  contactApi.ts  → createContact, getContacts, getContactById, updateContact, deleteContact
```

## Pages reference

**Persons table** — First Name, Last Name, Email, Gender, Address, Action (Edit/Delete icons).
**Persons form** — First Name, Last Name, Email, Address, Gender, Phone Number, Contact Type
(`PERSONAL|HOME|WORK|OTHER`).

**Contacts table** — Person Name, Phone Number, Contact Type, Action (Edit/Delete icons).
**Contacts form** — Person Name (dropdown from Persons API), Phone Number, Contact Type.

**Pagination footer** (both tables) — "Showing X of Y", Previous/Next, Previous disabled on page
1, Next disabled on the last page, requests the correct page from the backend.

**Excel upload** (Persons page) — select file → validate file type → call upload API → loading
state → success message → clear error message on failure → refresh both Persons and Contacts
tables on success.

## Validation rules

- Person: First Name required; Last Name required; Email required + valid; Gender required;
  Phone Number valid when provided; Contact Type required when a phone number is provided.
- Contact: Person required; Phone Number required; Contact Type required.

## Established conventions (undocumented in spec)

These aren't in `cmsPrompt.txt` but are real, followed consistently in the codebase — treat them
as rules, not just observations.

- Lint tool is `oxlint` (config: `.oxlintrc.json`), not ESLint — don't assume ESLint conventions
  or config file locations apply.
- Default page size is 6 (`PAGE_SIZE = 6` in `PersonsPage.tsx`/`ContactsPage.tsx`). The spec's
  `size=6` is only an example value in a URL, not a stated default — 6 is the actual rule to
  preserve.
- Search input is debounced 300ms before firing the API call.
- After delete/create redirects, success/warning banners are passed via React Router
  `location.state` and shown post-navigation — reuse this pattern for any new redirect+message
  flow rather than inventing a new one.
- Accepted Excel upload extensions: `.xlsx`, `.xls` (mirrors the backend-side check).
- The phone-number validation regex must match the backend's exactly:
  `^[0-9+()\-\s]{7,20}$` (see `contact-manager-backend`).

## Run / build commands

```bash
cd frontend
npm install            # install deps
npm run dev             # dev server, default http://localhost:5173
npm run typecheck       # or the project's configured TS validation command if not defined
npm run lint             # fix all lint errors before considering frontend complete
npm run build            # production build — must succeed with no TS/compile errors
npm run preview           # preview the production build
```

**Frontend verification** (must all succeed before calling the frontend done):

```bash
npm install
npm run lint
npm run typecheck
npm run build
```

# Campus Hiring Platform (Internship & Campus Hiring)

Java 17 + Spring Boot 3.x + PostgreSQL + Maven + React. No Python.

## Quick start
1. Copy `.env.example` to `.env`, set `DB_PASSWORD`, `JWT_SECRET` (32+ chars), `ADMIN_EMAIL`.
2. `docker compose up -d db` (PostgreSQL 15).
3. Backend: `cd backend && mvn spring-boot:run` → http://localhost:8080 (Swagger: `/swagger-ui.html`).
4. Frontend: `cd frontend && npm install && npm run dev` → http://localhost:5173.

## Business rules enforced + tested
- BR-01: one application per student+job (unique constraint + DuplicateException).
- BR-02: only OPEN jobs accept applications.
- BR-03: past-deadline applications rejected.
- BR-04: CGPA below job minimum rejected.
- BR-05: application status moves forward only (APPLIED→SHORTLISTED→INTERVIEW→SELECTED/REJECTED).
- BR-06: interviews scheduled in the future; scheduling auto-advances APPLIED→INTERVIEW.
- BR-07: single offer per application; PENDING→ACCEPTED/DECLINED.
- BR-08: match score bounded 0..1 (skill overlap + CGPA).
- BR-09: `/api/admin/**` requires ADMIN (SecurityConfig).
- BR-10: only STUDENT role can apply.

Tests: `PlacementWorkflowTest` (10 tests) run with H2 profile `test` (`mvn test`).

## API overview
- `POST /api/auth/register` (STUDENT/COMPANY only), `POST /api/auth/login` → JWT.
- `GET /api/jobs?q=&type=&location=` public; `POST /api/company/jobs` (COMPANY, verified only).
- `POST /api/applications`, `PATCH /api/applications/{id}/status`, `POST /api/applications/{id}/withdraw`.
- `POST /api/interviews`, `POST /api/offers`, `PATCH /api/offers/{id}/respond`.
- `GET /api/admin/stats`, `PATCH /api/admin/companies/{id}/verify`, `GET /api/health`.

## Docs
- `docs/ARCHITECTURE.md`, `docs/ER_DIAGRAM.md`, `docs/API.md`, `docs/TEST_REPORT.md`.

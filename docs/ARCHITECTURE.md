# Architecture

Layered monolith: `controller → service → repository → domain`, PostgreSQL via JPA, Flyway migrations (V1 schema, V2 admin seed), stateless JWT security, in-app notifications + best-effort email, audit log on auth/job/application/interview/offer/admin actions, springdoc OpenAPI.

Frontend: React + Vite + react-router, Axios with JWT interceptor, pages: Login/Register, Jobs search+apply, Company dashboard (post jobs), Admin stats.

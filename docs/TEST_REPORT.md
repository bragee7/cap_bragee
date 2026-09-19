# Test Report (verified 2026-09-19)

`PlacementWorkflowTest` — 10 tests mapping to BR-01..BR-10, run on H2
(`application-test.yml`, Flyway off, ddl create-drop).

Run: `mvn -f backend/pom.xml test` (Maven 3.9.9, JDK 25, `release 17`).

Result: **Tests run: 10, Failures: 0, Errors: 0 — BUILD SUCCESS.**

| # | Test | Business rule |
|---|------|---------------|
| BR-01 | br01_duplicateApplicationRejected | one application per student+job (unique constraint + service guard) |
| BR-02 | br02_closedJobRejectsApplications | only OPEN jobs accept applications |
| BR-03 | br03_pastDeadlineRejectsApplications | deadline enforced in service |
| BR-04 | br04_cgpaBelowMinimumRejected | CGPA below job minimum rejected |
| BR-05 | br05_stateMachineOnlyForward | forward-only application status transitions |
| BR-06 | br06_interviewInFutureAndAdvancesStatus | interviews in future; scheduling auto-advances APPLIED→INTERVIEW |
| BR-07 | br07_singleOfferPerApplication | single offer per application; PENDING→ACCEPTED/DECLINED |
| BR-08 | br08_matchScoreBounded | match score bounded 0..1 |
| BR-09 | br09_adminStatsPresent | admin stats endpoint returns platform counts |
| BR-10 | br10_onlyStudentCanApply | only STUDENT role can apply |

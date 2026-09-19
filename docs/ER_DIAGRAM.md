# ER Diagram

```mermaid
erDiagram
    USERS ||--o| STUDENTS : has
    USERS ||--o| COMPANIES : has
    COMPANIES ||--|{ JOB_POSTINGS : posts
    JOB_POSTINGS ||--|{ APPLICATIONS : receives
    STUDENTS ||--|{ APPLICATIONS : submits
    APPLICATIONS ||--o{ INTERVIEWS : schedules
    APPLICATIONS ||--o| OFFERS : results-in
    USERS ||--|{ AUDIT_LOGS : acts
```

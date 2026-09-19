CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_users_email ON users(email);

CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    college_name VARCHAR(180) NOT NULL,
    department VARCHAR(120) NOT NULL,
    graduation_year INTEGER NOT NULL,
    cgpa NUMERIC(4,2) NOT NULL,
    phone VARCHAR(20),
    resume_url VARCHAR(500),
    bio VARCHAR(2000),
    skills VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    company_name VARCHAR(180) NOT NULL,
    description VARCHAR(3000),
    website VARCHAR(300),
    industry VARCHAR(120),
    location VARCHAR(180),
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE job_postings (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(5000) NOT NULL,
    job_type VARCHAR(20) NOT NULL,
    location VARCHAR(180) NOT NULL,
    salary NUMERIC(12,2),
    required_skills VARCHAR(1000),
    minimum_cgpa NUMERIC(4,2),
    deadline TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_jobs_company ON job_postings(company_id);
CREATE INDEX idx_jobs_status ON job_postings(status);
CREATE INDEX idx_jobs_deadline ON job_postings(deadline);
CREATE INDEX idx_jobs_type ON job_postings(job_type);

CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    job_posting_id BIGINT NOT NULL REFERENCES job_postings(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'APPLIED',
    cover_letter VARCHAR(2000),
    resume_url VARCHAR(500),
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_application_student_job UNIQUE (student_id, job_posting_id)
);
CREATE INDEX idx_app_student ON applications(student_id);
CREATE INDEX idx_app_job ON applications(job_posting_id);
CREATE INDEX idx_app_status ON applications(status);

CREATE TABLE interviews (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    scheduled_at TIMESTAMP NOT NULL,
    mode VARCHAR(20) NOT NULL,
    meeting_link VARCHAR(500),
    interviewer_name VARCHAR(120),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    feedback VARCHAR(3000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_interview_app ON interviews(application_id);
CREATE INDEX idx_interview_status ON interviews(status);

CREATE TABLE offers (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL UNIQUE REFERENCES applications(id) ON DELETE CASCADE,
    salary NUMERIC(12,2),
    joining_date DATE,
    offer_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_offer_application UNIQUE (application_id)
);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(60),
    entity_id BIGINT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_notif_user ON notifications(user_id);

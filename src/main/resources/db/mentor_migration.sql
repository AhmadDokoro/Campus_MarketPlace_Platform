-- Run this manually in MySQL before starting the app (ddl-auto=validate)

-- 1) Create mentors table
CREATE TABLE IF NOT EXISTS mentors (
    mentor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mentor_name VARCHAR(150) NOT NULL,
    mentor_email VARCHAR(255) NOT NULL,
    CONSTRAINT uk_mentor_email UNIQUE (mentor_email)
);

-- 2) Add mentor_id to users table (nullable)
ALTER TABLE users
    ADD COLUMN mentor_id BIGINT NULL;

-- 3) Add foreign key constraint
ALTER TABLE users
    ADD CONSTRAINT fk_user_mentor
    FOREIGN KEY (mentor_id) REFERENCES mentors(mentor_id);

-- 4) Drop old mentor columns from users (since you will wipe old records)
ALTER TABLE users
    DROP COLUMN mentor_name,
    DROP COLUMN mentor_email;


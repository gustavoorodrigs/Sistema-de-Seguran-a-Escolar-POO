CREATE TABLE IF NOT EXISTS students (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    birth_date DATE NOT NULL,
    class_name VARCHAR(50) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    badge_code VARCHAR(50) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_students_full_name ON students(full_name);
CREATE INDEX IF NOT EXISTS idx_students_class_name ON students(class_name);
CREATE INDEX IF NOT EXISTS idx_students_active ON students(active);
CREATE INDEX IF NOT EXISTS idx_students_badge_code ON students(badge_code);

CREATE TABLE IF NOT EXISTS student_guardians (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    guardian_user_id BIGINT NOT NULL,
    relationship VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_sg_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE RESTRICT,
    CONSTRAINT fk_sg_guardian FOREIGN KEY (guardian_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT uk_student_guardian UNIQUE (student_id, guardian_user_id)
);

CREATE INDEX IF NOT EXISTS idx_sg_student_id ON student_guardians(student_id);
CREATE INDEX IF NOT EXISTS idx_sg_guardian_id ON student_guardians(guardian_user_id);

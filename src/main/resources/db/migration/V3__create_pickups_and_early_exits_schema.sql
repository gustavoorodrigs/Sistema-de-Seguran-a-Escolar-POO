CREATE TABLE IF NOT EXISTS authorized_pickups (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    relationship VARCHAR(50) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    document_number VARCHAR(30),
    status VARCHAR(20) NOT NULL DEFAULT 'VALIDA',
    valid_until DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_ap_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE RESTRICT,
    CONSTRAINT chk_ap_status CHECK (status IN ('VALIDA', 'VENCIDA', 'REVOGADA'))
);

CREATE INDEX IF NOT EXISTS idx_ap_student_id ON authorized_pickups(student_id);
CREATE INDEX IF NOT EXISTS idx_ap_status ON authorized_pickups(status);

CREATE TABLE IF NOT EXISTS pickup_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    authorized_pickup_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    changed_by_user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    previous_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_pal_pickup FOREIGN KEY (authorized_pickup_id) REFERENCES authorized_pickups(id) ON DELETE RESTRICT,
    CONSTRAINT fk_pal_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE RESTRICT,
    CONSTRAINT fk_pal_user FOREIGN KEY (changed_by_user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_pal_pickup_id ON pickup_audit_logs(authorized_pickup_id);
CREATE INDEX IF NOT EXISTS idx_pal_student_id ON pickup_audit_logs(student_id);

CREATE TABLE IF NOT EXISTS early_exits (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    authorized_pickup_id BIGINT NOT NULL,
    authorized_by_user_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    exit_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_ee_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ee_pickup FOREIGN KEY (authorized_pickup_id) REFERENCES authorized_pickups(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ee_user FOREIGN KEY (authorized_by_user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_ee_student_id ON early_exits(student_id);
CREATE INDEX IF NOT EXISTS idx_ee_pickup_id ON early_exits(authorized_pickup_id);

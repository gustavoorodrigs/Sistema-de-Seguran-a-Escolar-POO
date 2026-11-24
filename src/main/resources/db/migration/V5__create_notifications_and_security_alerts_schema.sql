CREATE TABLE IF NOT EXISTS guardian_notifications (
    id BIGSERIAL PRIMARY KEY,
    guardian_user_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(500) NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_gn_guardian FOREIGN KEY (guardian_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_gn_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_gn_guardian_created ON guardian_notifications(guardian_user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_gn_guardian_unread ON guardian_notifications(guardian_user_id, is_read);

CREATE TABLE IF NOT EXISTS security_alerts (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT,
    device_id BIGINT,
    severity VARCHAR(20) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ABERTO',
    resolution_notes VARCHAR(500),
    resolved_by_email VARCHAR(150),
    resolved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_sa_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL,
    CONSTRAINT fk_sa_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE SET NULL,
    CONSTRAINT chk_sa_severity CHECK (severity IN ('BAIXA', 'MEDIA', 'ALTA')),
    CONSTRAINT chk_sa_status CHECK (status IN ('ABERTO', 'RESOLVIDO'))
);

CREATE INDEX IF NOT EXISTS idx_sa_status_created ON security_alerts(status, created_at DESC);
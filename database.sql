CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_user_role CHECK (role IN ('COORDENADOR', 'SUPERVISOR', 'RESPONSAVEL', 'ALUNO'))
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

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

CREATE TABLE IF NOT EXISTS devices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    device_type VARCHAR(30) NOT NULL,
    location VARCHAR(150) NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_device_type CHECK (device_type IN ('CATRACA', 'CAMERA', 'LEITOR_PORTARIA'))
);

CREATE INDEX IF NOT EXISTS idx_devices_token_hash ON devices(token_hash);
CREATE INDEX IF NOT EXISTS idx_devices_active ON devices(active);

CREATE TABLE IF NOT EXISTS access_logs (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT,
    device_id BIGINT NOT NULL,
    direction VARCHAR(20) NOT NULL,
    validation_status VARCHAR(30) NOT NULL,
    denial_reason VARCHAR(255),
    raw_identifier VARCHAR(100) NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_al_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE RESTRICT,
    CONSTRAINT fk_al_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE RESTRICT,
    CONSTRAINT chk_al_direction CHECK (direction IN ('ENTRADA', 'SAIDA')),
    CONSTRAINT chk_al_validation_status CHECK (validation_status IN ('AUTORIZADO', 'NEGADO', 'VERIFICACAO_PENDENTE'))
);

CREATE INDEX IF NOT EXISTS idx_al_student_id ON access_logs(student_id);
CREATE INDEX IF NOT EXISTS idx_al_device_id ON access_logs(device_id);
CREATE INDEX IF NOT EXISTS idx_al_recorded_at ON access_logs(recorded_at DESC);
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
CREATE TABLE IF NOT EXISTS school_announcements (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(40) NOT NULL DEFAULT 'GERAL',
    author_name VARCHAR(150) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_announcements_created_at ON school_announcements(created_at DESC);

INSERT INTO school_announcements (title, content, category, author_name)
VALUES
    ('Protocolo de IdentificaÃ§Ã£o nas Catracas', 'Lembramos a todos os alunos e responsÃ¡veis que o uso do crachÃ¡ escolar Ã© obrigatÃ³rio na entrada e na saÃ­da em todas as portarias.', 'SEGURANCA', 'CoordenaÃ§Ã£o Escolar'),
    ('ReuniÃ£o de Pais e Mestres - Bimestre', 'A reuniÃ£o bimestral de acompanhamento pedagÃ³gico e seguranÃ§a ocorrerÃ¡ nesta sexta-feira Ã s 18h30 no auditÃ³rio principal.', 'EVENTO', 'Secretaria AcadÃªmica');

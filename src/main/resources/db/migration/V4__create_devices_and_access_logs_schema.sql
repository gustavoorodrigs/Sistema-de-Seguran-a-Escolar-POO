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
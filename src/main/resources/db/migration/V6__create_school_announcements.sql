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
    ('Protocolo de Identificação nas Catracas', 'Lembramos a todos os alunos e responsáveis que o uso do crachá escolar é obrigatório na entrada e na saída em todas as portarias.', 'SEGURANCA', 'Coordenação Escolar'),
    ('Reunião de Pais e Mestres - Bimestre', 'A reunião bimestral de acompanhamento pedagógico e segurança ocorrerá nesta sexta-feira às 18h30 no auditório principal.', 'EVENTO', 'Secretaria Acadêmica');
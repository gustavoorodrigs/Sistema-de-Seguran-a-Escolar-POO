# SPECS — Especificações Técnicas e Contratos de API
## Projeto: SSE (Sistema de Segurança Escolar)

## 1. Modelo de Dados (PostgreSQL / Flyway V1–V6)
- **`users` (V1):** Credenciais (`email`, `password_hash` BCrypt) e perfil (`COORDENADOR`, `SUPERVISOR`, `RESPONSAVEL`, `ALUNO`).
- **`students` e `student_guardians` (V2):** Cadastro de alunos, `badge_code` único, status `active` e vínculo N:N com responsáveis.
- **`authorized_pickups`, `early_exits` e `pickup_audit_logs` (V3):** Autorizações (`VALIDA`, `REVOGADA`, `EXPIRADA`), saídas antecipadas e auditoria.
- **`devices` e `access_logs` (V4):** Dispositivos IoT (`ENTRADA`, `SAIDA`, `BIDIRECIONAL`), token de hardware e logs (`AUTORIZADO`, `NEGADO`).
- **`guardian_notifications` e `security_alerts` (V5):** Notificações familiares e incidentes (`BAIXA`, `MEDIA`, `ALTA`, `CRITICA`) com status (`ABERTO`, `RESOLVIDO`).
- **`school_announcements` (V6):** Comunicados institucionais publicados pela coordenação.

## 2. Contratos Principais da API REST
- **Autenticação:** `POST /api/auth/login` (retorna JWT, nome, e-mail e role).
- **Dispositivos IoT (`X-Device-Token`):** `POST /api/devices/access` (valida matrícula ativa, aplica anti-passback, registra log, notifica responsáveis ou abre alerta).
- **Administração (`/api/admin/**`):** Gestão de alunos (`/students`), autorizações (`/authorizations`), saídas antecipadas (`/early-exits`), catracas (`/devices`), alertas (`/alerts`), indicadores (`/reports/summary`), histórico filtrável (`/reports/access-logs`), exportação CSV (`/reports/access-logs/export.csv`) e comunicados (`/announcements`).
- **Portal Familiar (`/api/guardian/**`):** Dependentes (`/students`), autorizações (`/authorizations`), notificações (`/notifications`) e comunicados (`/announcements`).
- **Portal do Aluno (`/api/student/**`):** Painel unificado (`/dashboard`) com dados do crachá, histórico individual e comunicados.
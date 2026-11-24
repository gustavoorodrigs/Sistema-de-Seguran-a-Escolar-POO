# PRD — Product Requirements Document
## Produto: SSE (Sistema de Segurança Escolar)
**Versão:** 1.0 | **Status:** Concluído (Fases 1 a 6)

## 1. Visão Geral e Problema
O **SSE (Sistema de Segurança Escolar)** integra hardware de controle de acesso (catracas e leitores RFID) a uma plataforma web centralizada com isolamento estrito por perfil de usuário, eliminando controles manuais e mitigando riscos de retiradas não autorizadas.

## 2. Personas e Perfis de Acesso
- **Coordenador Escolar (`COORDENADOR`):** Gestão completa de alunos, vínculos familiares, dispositivos IoT, comunicados, alertas e relatórios.
- **Supervisor de Portaria (`SUPERVISOR`):** Operação de portaria, saídas antecipadas, catracas, alertas e relatórios.
- **Responsável Legal (`RESPONSAVEL`):** Dependentes vinculados, pessoas autorizadas para retirada, notificações em tempo real e comunicados.
- **Aluno (`ALUNO`):** Status da matrícula e crachá RFID, histórico individual de catraca e comunicados institucionais.

## 3. Requisitos Funcionais (RF)
- **RF001 — Autenticação e RBAC:** Login institucional com JWT stateless e bloqueio de rotas por perfil.
- **RF002 — Gestão de Alunos e Crachás:** Cadastro, edição, filtragem, bloqueio preventivo e crachá RFID único (`badge_code`).
- **RF003 — Gestão de Dispositivos IoT:** Cadastro de catracas com token dedicado (`X-Device-Token`).
- **RF004 — Validação de Acesso em Tempo Real:** Processamento de `ENTRADA` e `SAIDA`, prevenção de duplicidade (anti-passback) e log imutável.
- **RF005 — Pessoas Autorizadas para Retirada:** Cadastro, revogação, validade e trilha de auditoria.
- **RF006 — Saídas Antecipadas:** Liberação condicionada exclusivamente a autorização com status `VALIDA`.
- **RF007 — Relatórios Gerenciais e Exportação CSV:** Ocupação escolar em tempo real, histórico filtrável e exportação CSV (UTF-8 com BOM).
- **RF008 — Central de Alertas:** Geração automática de incidentes (crachá bloqueado/desconhecido) com severidade e tratativa.
- **RF009 — Portal Familiar e Notificações:** Isolamento por vínculo (`student_guardians`) e alertas automáticos de passagem na catraca.
- **RF010 — Portal do Aluno e Comunicados:** Publicação de avisos pela coordenação e consulta individual pelos alunos.

## 4. Requisitos de Segurança e Não Funcionais
- **RS001 — Isolamento Familiar:** Responsáveis acessam apenas dependentes vinculados à sua conta.
- **RS002 — Bloqueio de Retirada Não Autorizada:** Saídas antecipadas são bloqueadas no servidor sem autorização `VALIDA`.
- **RS003 — Segregação de Rotas:** Endpoints `/api/admin/**`, `/api/guardian/**`, `/api/student/**` e `/api/devices/**` isolados no Spring Security.
- **RNF001 — Versionamento de Banco:** Esquema e carga inicial gerenciados via Flyway (`V1` a `V6`).
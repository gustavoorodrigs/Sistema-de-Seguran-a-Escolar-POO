# SPEC 06 — Histórico de Operações, Filtros e Dashboard

**Módulo Java:** `com.school.security.reports`  
**Requisito Relacionado:** RF07, RNF05

## 1. Objetivo
Fornecer visão consolidada da segurança escolar e consultas filtradas sobre o histórico de acessos para auditoria administrativa.

## 2. Atores
* **Administrador (`ADMIN`):** Opera os indicadores e filtros através do painel `dashboard.html`.

## 3. Regras de Negócio
* O dashboard consolida contadores de entradas no dia, saídas realizadas, bloqueios na catraca e alertas pendentes.
* A busca histórica suporta filtros combinados por intervalo de datas, matrícula/nome do aluno, dispositivo e status (`granted`).

## 4. Endpoints Principais
* `GET /api/admin/reports/summary` — Retorna métricas consolidadas para os cards do dashboard.
* `GET /api/admin/reports/access-logs` — Retorna histórico paginado e filtrado de movimentações.

## 5. Classes POO Envolvidas
* Utiliza polimorfismo sobre `RegistroAuditavel` para formatar resumos uniformes de eventos (`resumirEvento()`) na trilha de auditoria.
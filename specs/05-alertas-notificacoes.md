# SPEC 05 — Central de Alertas de Segurança e Notificações

**Módulo Java:** `com.school.security.notifications`  
**Requisito Relacionado:** RF05, RF06

## 1. Objetivo
Centralizar incidentes críticos de segurança para tratamento pela coordenação escolar e manter os responsáveis informados em tempo real sobre movimentações e ocorrências dos alunos.

## 2. Atores
* **Administrador (`ADMIN`):** Visualiza, filtra e encerra alertas de segurança na central.
* **Responsável (`GUARDIAN`):** Consulta notificações vinculadas aos seus dependentes.

## 3. Regras de Negócio
* Bloqueios de saída não autorizada e tentativas com crachá bloqueado geram alertas com severidade `HIGH`.
* Um alerta com status `RESOLVED` não pode ser reaberto ou resolvido novamente.

## 4. Endpoints Principais
* `GET /api/admin/alerts` — Lista alertas de segurança abertos e resolvidos.
* `PATCH /api/admin/alerts/{id}/resolve` — Registra notas de resolução e encerra o alerta.
* `GET /api/guardian/notifications` — Lista notificações destinadas ao responsável autenticado.

## 5. Classes POO Envolvidas
* `Notificavel` (interface): Contrato polimórfico implementado por `SecurityAlert` e `AccessLog`.
* `SecurityAlert`: Estende `RegistroAuditavel` e encapsula `resolver(User admin, String notas)`.
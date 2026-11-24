# SPEC 04 — Autorizações de Saída Antecipada e Terceiros

**Módulo Java:** `com.school.security.authorizations`  
**Requisito Relacionado:** RF04, RN03

## 1. Objetivo
Gerenciar solicitações de saída antecipada de estudantes e o cadastro de pessoas autorizadas para retirada na portaria, garantindo rastreabilidade e aprovação formal.

## 2. Atores
* **Responsável (`GUARDIAN`):** Solicita saída antecipada e cadastra terceiros autorizados (`authorized_pickups`).
* **Administrador (`ADMIN`):** Aprova ou rejeita solicitações pendentes.

## 3. Regras de Negócio
* **RN03 (Máquina de Estados):** Uma `EarlyExit` nasce como `PENDING`. Apenas solicitações `PENDING` podem ser aprovadas (`APPROVED`) ou rejeitadas (`REJECTED`). Após o uso na catraca, o estado muda para `USED`.
* Autorizações vencidas ou já consumidas falham na validação `validarRegraNegocio()`.

## 4. Endpoints Principais
* `POST /api/guardian/early-exits` — Cria solicitação de saída antecipada.
* `PATCH /api/admin/early-exits/{id}/approve` — Aprova solicitação pendente.
* `PATCH /api/admin/early-exits/{id}/reject` — Rejeita solicitação com justificativa.
* `POST /api/guardian/authorized-pickups` — Cadastra pessoa autorizada para retirada.

## 5. Classes POO Envolvidas
* `EarlyExit`: Estende `RegistroAuditavel` e implementa `Validavel`, encapsulando `aprovar()`, `rejeitar()` e `consumirNaCatraca()`.
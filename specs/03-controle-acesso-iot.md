# SPEC 03 — Controle de Acesso em Tempo Real (Catracas IoT)

**Módulo Java:** `com.school.security.accesscontrol`  
**Requisito Relacionado:** RF03, RN02, RN04

## 1. Objetivo
Processar em tempo real os eventos de leitura RFID enviados pelas catracas da escola, decidindo pela liberação ou bloqueio da passagem e gravando a trilha de auditoria.

## 2. Atores
* **Catraca / Leitor RFID (`DEVICE`):** Envia o identificador do dispositivo, o código RFID lido e a direção (`ENTRADA` ou `SAIDA`).

## 3. Fluxo Funcional
1. O leitor envia `POST /api/devices/access` com `X-Device-Key`.
2. O sistema valida se o `Device` está ativo (**RN04**) e localiza o `Student` pelo `rfidTag`.
3. Se `direction == ENTRADA` e o crachá estiver ativo, o acesso é autorizado (`granted = true`).
4. Se `direction == SAIDA`, o sistema verifica se o aluno possui permissão de saída livre ou uma `EarlyExit` com status `APPROVED` (**RN02**).
5. O resultado (autorizado ou negado) é persistido em `access_logs`.
6. Em caso de negação, dispara automaticamente um `SecurityAlert` e uma `GuardianNotification`.

## 4. Endpoints Principais
* `POST /api/devices/access` — Registra e valida passagem na catraca.

## 5. Classes POO Envolvidas
* `RegistroAuditavel` (abstrata): Fornece `id`, `createdAt` e contrato `resumirEvento()`.
* `AccessLog`: Estende `RegistroAuditavel` e implementa `Notificavel` de forma polimórfica.
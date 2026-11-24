# SPEC 02 — Gestão de Alunos, Responsáveis e Crachás RFID

**Módulo Java:** `com.school.security.students`  
**Requisito Relacionado:** RF02, RN01

## 1. Objetivo
Permitir o cadastro completo de estudantes, o vínculo com seus responsáveis legais e o gerenciamento do ciclo de vida dos crachás RFID utilizados nas catracas.

## 2. Atores
* **Administrador (`ADMIN`):** Realiza cadastro, atualização, vínculo de responsáveis e ativação/bloqueio de crachás.

## 3. Regras de Negócio
* **RN01:** Cada código `rfidTag` ativo deve ser único no sistema; não é permitido vincular a mesma tag a dois alunos ativos simultaneamente.
* O bloqueio de um crachá (`bloquearCracha()`) impede imediatamente novas passagens nas catracas sem apagar o histórico anterior do aluno.
* Um aluno pode estar associado a um ou mais responsáveis (`student_guardians`).

## 4. Endpoints Principais
* `GET /api/admin/students` — Lista estudantes cadastrados com status do crachá.
* `POST /api/admin/students` — Cadastra novo aluno e opcionalmente vincula crachá RFID.
* `PATCH /api/admin/students/{id}/rfid/block` — Bloqueia o crachá do estudante.

## 5. Classes POO Envolvidas
* `Pessoa` (superclasse abstrata): Define atributos comuns (`fullName`, `documentNumber`, `active`) e contrato `getIdentificacao()`.
* `Student` (subclasse): Estende `Pessoa`, encapsulando `vincularCracha(String rfid)` e `bloquearCracha()`.
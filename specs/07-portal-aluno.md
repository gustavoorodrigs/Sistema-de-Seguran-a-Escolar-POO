# SPEC 07 — Portal do Aluno e Comunicados Institucionais

**Módulo Java:** `com.school.security.studentportal`  
**Requisito Relacionado:** RF08, RN05

## 1. Objetivo
Disponibilizar um ambiente exclusivo para o estudante acompanhar sua própria frequência, situação do crachá RFID, saídas autorizadas e avisos publicados pela coordenação.

## 2. Atores
* **Aluno (`STUDENT`):** Consulta seus dados pessoais e comunicados.
* **Administrador (`ADMIN`):** Publica e gerencia comunicados institucionais (`school_announcements`).

## 3. Regras de Negócio
* **RN05 (Isolamento de Dados):** Todas as consultas em `/api/student/**` extraem a identidade do usuário diretamente do token JWT, impedindo que um aluno consulte dados de outro colega por manipulação de ID na URL.

## 4. Endpoints Principais
* `GET /api/student/profile` — Retorna dados acadêmicos e status atual do crachá RFID.
* `GET /api/student/access-history` — Lista entradas e saídas do aluno autenticado.
* `GET /api/student/announcements` — Lista comunicados ativos da instituição.
* `POST /api/admin/announcements` — Publica novo comunicado institucional.

## 5. Classes POO Envolvidas
* `Student`: Fornece dados encapsulados de matrícula, turma e estado do crachá para exibição no portal.
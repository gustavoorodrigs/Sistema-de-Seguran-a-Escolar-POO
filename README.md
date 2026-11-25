# Sistema Integrado de Segurança e Controle de Acesso Escolar

Sistema web e API RESTful desenvolvida em **Java + Spring Boot + PostgreSQL** para automação de portaria escolar (catracas RFID), gestão de saídas antecipadas, central de alertas de segurança e portal do aluno.

---

## 1. Arquitetura e Pilares de POO (60% da Avaliação)

O modelo de domínio foi estruturado para aplicar explicitamente os quatro pilares da Programação Orientada a Objetos:

| Pilar de POO | Implementação no Código-Fonte | Arquivos Principais |
| :--- | :--- | :--- |
| **Abstração** | Superclasses abstratas `@MappedSuperclass` e interfaces de contrato comportamental. | `shared/domain/Pessoa.java`<br>`shared/domain/RegistroAuditavel.java`<br>`shared/domain/Notificavel.java`<br>`shared/domain/Validavel.java` |
| **Herança** | Especialização das entidades de negócio herdando atributos e comportamentos base. | `students/model/Student.java`<br>`accesscontrol/model/AccessLog.java`<br>`notifications/model/SecurityAlert.java`<br>`authorizations/model/EarlyExit.java` |
| **Encapsulamento** | Regras de negócio e validação de estado protegidas dentro das próprias entidades. | `Student.vincularCracha()` / `bloquearCracha()`<br>`SecurityAlert.resolve()`<br>`EarlyExit.validarRegraNegocio()` |
| **Polimorfismo** | Sobrescrita de métodos (`@Override`) e processamento genérico sem condicionais por tipo. | `reports/service/AuditoriaPolimorficaService.java` |

---

## 2. Funcionalidades Operacionais Implementadas (8 Fluxos)

1. **Autenticação, RBAC e Segurança da Conta:** Login JWT por perfil (`COORDENADOR`, `SUPERVISOR`, `RESPONSAVEL`, `ALUNO`), autenticação de catracas via `X-Device-Key` e redefinição de senha provisória pelo próprio usuário (`PATCH /api/auth/password`).
2. **Gestão de Alunos, Responsáveis e Crachás RFID:** Cadastro de estudantes, criação e ativação/desativação de contas de responsáveis familiares pela coordenação (`/api/admin/guardians`), vínculo dinâmico entre responsável e aluno e ativação/bloqueio de credenciais RFID.
3. **Controle de Acesso em Tempo Real:** Processamento de passagens (`ENTRADA`/`SAIDA`) nas catracas com validação de regras de horário e autorização.
4. **Saídas Antecipadas e Retiradas:** Cadastro de terceiros autorizados e fluxo de solicitação/aprovação de saída antecipada.
5. **Central de Alertas de Segurança:** Geração automática de incidentes em tentativas bloqueadas e fluxo de resolução pela coordenação.
6. **Notificações aos Responsáveis:** Registro de avisos automáticos sobre movimentações e ocorrências dos dependentes.
7. **Relatórios e Dashboard Administrativo:** Indicadores consolidados e histórico auditável com filtros.
8. **Portal do Aluno e Comunicados:** Consulta individual de frequência, crachá e mural de avisos institucionais.

---

## 3. Documentação do Projeto

* **PRD (Documento de Requisitos):** [`prd.md`](./prd.md)
* **Arquitetura e Diagrama de Classes:** [`docs/arquitetura.md`](./docs/arquitetura.md)
* **Especificações Funcionais:** [`specs/`](./specs/) (`01` a `07`)
* **Skills de Desenvolvimento:** [`skills/`](./skills/)
* **Script do Banco de Dados:** [`database.sql`](./database.sql)

---

## 4. Como Executar o Projeto Localmente

### Pré-requisitos
- Java 17 ou superior (testado com JDK 25)
- Apache Maven 3.9+
- Banco de dados H2 em memória (perfil padrão `dev`) ou PostgreSQL 14+ (perfil `prod`)

### Passo a Passo
1. **Executar em modo de desenvolvimento (H2 em memória com dados iniciais):**
   ```
bash
   mvn spring-boot:run
   ```
2. **Ou executar com PostgreSQL (perfil `prod`):**
   ```
sql
   CREATE DATABASE school_security;
   ```
   ```
bash
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```
3. **Acessar a aplicação:**
   Abra `http://localhost:8080` no navegador.

---

## 5. Endpoints de Autenticação e Gestão de Responsáveis

| Método | Endpoint | Perfis Permitidos | Descrição |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Público | Autentica o usuário e retorna o token JWT. |
| `GET` | `/api/auth/me` | Autenticado | Retorna os dados do perfil autenticado. |
| `PATCH` | `/api/auth/password` | Autenticado | Substitui a senha atual (provisória) por uma nova senha pessoal. |
| `GET` | `/api/admin/guardians` | `COORDENADOR`, `SUPERVISOR` | Lista os responsáveis familiares e seus alunos vinculados. |
| `POST` | `/api/admin/guardians` | `COORDENADOR`, `SUPERVISOR` | Cadastra uma conta de responsável (`RESPONSAVEL`) com vínculo opcional imediato a um aluno. |
| `PATCH` | `/api/admin/guardians/{id}/status` | `COORDENADOR`, `SUPERVISOR` | Ativa ou desativa o acesso de um responsável familiar. |
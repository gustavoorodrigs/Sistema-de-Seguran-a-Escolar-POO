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

1. **Autenticação e Controle de Acesso (RBAC & IoT):** Login JWT por perfil (`ADMIN`, `GUARDIAN`, `STUDENT`) e autenticação de catracas via `X-Device-Key`.
2. **Gestão de Alunos e Crachás RFID:** Cadastro de estudantes, vínculo de responsáveis e ativação/bloqueio de credenciais RFID.
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
* Java 17 ou superior
* PostgreSQL 14+

### Passo a Passo
1. **Criar o banco de dados no PostgreSQL:**
   ```sql
   CREATE DATABASE school_security;
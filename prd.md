# PRD — Sistema Integrado de Segurança e Controle de Acesso Escolar (`school-security-system`)

**Versão:** 1.0  
**Disciplina:** Programação Orientada a Objetos (2026.2)  
**Stack Tecnológica:** Java 17+, Spring Boot, Spring Security (JWT), Spring Data JPA / Hibernate, PostgreSQL, Flyway, HTML5/CSS3/JavaScript.

---

## 1. Visão Geral do Produto

### 1.1 Problema
Instituições de ensino enfrentam falhas críticas na rastreabilidade de entrada e saída de estudantes, dependência de registros manuais em portarias, vulnerabilidades na validação de terceiros autorizados a buscar alunos e demora na comunicação de ocorrências entre a equipe escolar e os responsáveis legais.

### 1.2 Objetivo
Desenvolver um sistema web e API RESTful orientada a objetos para automatizar o controle de acesso físico (via catracas e crachás RFID), gerenciar autorizações de saída antecipada, emitir alertas de segurança em tempo real para a coordenação e disponibilizar um portal transparente para alunos e responsáveis.

### 1.3 Escopo do Sistema
O sistema opera como um monólito modular com persistência relacional em PostgreSQL e autenticação baseada em papéis (RBAC), integrando dispositivos de borda (catracas/leitores IoT), painel administrativo web (`dashboard.html`) e portais específicos por perfil de usuário.

---

## 2. Personas e Perfis de Acesso (RBAC)

| Perfil / Role | Ator | Responsabilidades Principais |
| :--- | :--- | :--- |
| **`ADMIN`** | Coordenação / Direção Escolar | Cadastrar alunos, vincular responsáveis, gerenciar dispositivos/catracas, auditar logs de acesso, tratar alertas de segurança e publicar comunicados institucionais. |
| **`GUARDIAN`** | Pai, Mãe ou Responsável Legal | Receber notificações de entrada/saída, cadastrar terceiros autorizados para retirada (`authorized_pickups`) e solicitar/aprovar saídas antecipadas (`early_exits`). |
| **`STUDENT`** | Aluno Matriculado | Acessar o Portal do Aluno para consultar histórico de frequência, status do crachá RFID, solicitações de saída e comunicados da escola. |
| **`DEVICE` (IoT)** | Catraca / Leitor RFID | Autenticar-se via chave de dispositivo dedicada (`/api/devices`) e enviar eventos de leitura de crachá em tempo real para validação de entrada ou saída. |

---

## 3. Requisitos Funcionais (8 Funcionalidades Operacionais)

> *Nota: O projeto implementa 8 funcionalidades completas ponta a ponta (interface/API, regras de negócio orientadas a objetos e persistência em banco), superando o mínimo de 7 funcionalidades exigidas.*

* **RF01 — Autenticação e Controle de Acesso (RBAC & IoT):**  
  O sistema deve autenticar usuários humanos via credenciais e emitir tokens JWT contendo o perfil de acesso (`ADMIN`, `GUARDIAN`, `STUDENT`), além de validar requisições de hardware de portaria por meio de filtro de chave de dispositivo (`X-Device-Key`).
* **RF02 — Gestão de Alunos, Responsáveis e Crachás RFID:**  
  O administrador deve poder cadastrar estudantes, associar responsáveis legais (`student_guardians`) e vincular ou bloquear códigos de crachá RFID únicos por aluno.
* **RF03 — Controle de Acesso em Tempo Real (Catracas IoT):**  
  Ao receber a leitura de um crachá RFID por um dispositivo ativo, o sistema deve validar o status do aluno, verificar restrições de saída antecipada e registrar o evento de `ENTRADA` ou `SAIDA` na tabela `access_logs`.
* **RF04 — Gestão de Autorizações de Saída Antecipada e Retirada:**  
  Responsáveis e administradores devem poder registrar solicitações de saída antecipada (`early_exits`) e cadastrar pessoas autorizadas para retirada (`authorized_pickups`), exigindo aprovação formal antes da liberação na catraca.
* **RF05 — Central de Alertas de Segurança:**  
  Tentativas de acesso negadas (crachá inválido, bloqueado ou saída não autorizada) devem gerar automaticamente registros em `security_alerts` com classificação de severidade para triagem e resolução pela coordenação.
* **RF06 — Notificações Automáticas aos Responsáveis:**  
  Sempre que um aluno registrar movimentação na portaria ou tiver uma ocorrência crítica, o sistema deve gerar uma notificação direcionada ao responsável (`guardian_notifications`).
* **RF07 — Histórico de Operações, Filtros e Relatórios:**  
  O sistema deve disponibilizar consultas filtradas (por data, aluno, dispositivo, tipo de movimentação e status) e consolidação de indicadores operacionais no painel administrativo (`dashboard.html`).
* **RF08 — Portal do Aluno e Comunicados Institucionais:**  
  O estudante deve visualizar seus próprios registros de acesso, status de autorização e os comunicados publicados pela escola (`school_announcements`).

---

## 4. Requisitos Não Funcionais

* **RNF01 — Paradigma Orientado a Objetos:** O domínio da aplicação deve aplicar explicitamente Encapsulamento, Herança, Polimorfismo e Abstração na modelagem das entidades e regras de negócio.
* **RNF02 — Persistência e Integridade de Dados:** Todos os dados devem ser persistidos no PostgreSQL utilizando Spring Data JPA / Hibernate com versionamento de schema via Flyway e script `.sql` consolidado para implantação.
* **RNF03 — Segurança de Endpoints:** Endpoints administrativos (`/api/admin/**`), familiares (`/api/guardian/**`) e estudantis (`/api/student/**`) devem ser protegidos por Spring Security com validação stateless via JWT.
* **RNF04 — Auditabilidade:** Toda tentativa de acesso físico (autorizada ou negada) e toda mudança de status em alertas ou autorizações deve preservar carimbo de data/hora (`timestamp`) e autoria.
* **RNF05 — Usabilidade do Painel:** O frontend administrativo (`dashboard.html` + `app.js`) deve permitir operação completa dos fluxos principais sem necessidade de ferramentas externas de API.

---

## 5. Regras de Negócio

* **RN01 (Unicidade de Crachá):** Um código RFID ativo não pode estar associado simultaneamente a dois alunos diferentes.
* **RN02 (Bloqueio de Saída sem Autorização):** Durante o horário letivo, a leitura de saída de um aluno sem registro aprovado em `early_exits` deve bloquear a catraca, negar o acesso no `access_logs` e disparar um `SecurityAlert` de severidade `HIGH`.
* **RN03 (Ciclo de Vida de Autorização):** Uma solicitação em `early_exits` inicia com status `PENDING`, podendo transitar apenas para `APPROVED` ou `REJECTED` por um usuário autorizado, e para `USED` após a passagem na catraca.
* **RN04 (Dispositivo Ativo):** Leituras oriundas de dispositivos marcados como inativos (`active = false`) devem ser rejeitadas imediatamente.
* **RN05 (Isolamento de Dados do Aluno):** Um usuário com perfil `STUDENT` só pode consultar os registros vinculados ao seu próprio identificador de matrícula.

---

## 6. Rastreabilidade (Funcionalidades × Módulos × Specs)

| ID | Funcionalidade | Pacote Java | Prefixos de Rota | Especificação |
| :--- | :--- | :--- | :--- | :--- |
| RF01 | Autenticação e RBAC | `auth` | `/api/auth/**` | `specs/01-autenticacao-rbac.md` |
| RF02 | Alunos, Responsáveis e RFID | `students` | `/api/admin/students/**` | `specs/02-gestao-alunos-rfid.md` |
| RF03 | Controle de Acesso IoT | `accesscontrol` | `/api/devices/**` | `specs/03-controle-acesso-iot.md` |
| RF04 | Saídas Antecipadas e Retiradas | `authorizations` | `/api/guardian/**`, `/api/admin/**` | `specs/04-autorizacoes-saida.md` |
| RF05 | Alertas de Segurança | `notifications` | `/api/admin/alerts/**` | `specs/05-alertas-notificacoes.md` |
| RF06 | Notificações Familiares | `notifications` | `/api/guardian/notifications/**` | `specs/05-alertas-notificacoes.md` |
| RF07 | Relatórios e Dashboard | `reports` | `/api/admin/reports/**` | `specs/06-relatorios-dashboard.md` |
| RF08 | Portal do Aluno e Comunicados | `studentportal` | `/api/student/**` | `specs/07-portal-aluno.md` |
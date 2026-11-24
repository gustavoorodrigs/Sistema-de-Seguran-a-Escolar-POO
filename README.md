# SSE — Sistema de Segurança Escolar

Sistema web full-stack para controle de acesso físico, gestão de matrículas e crachás RFID, autorização de retirada de alunos, monitoramento de catracas IoT em tempo real, central de alertas de segurança, relatórios gerenciais e portal integrado para famílias e estudantes.

---

## Arquitetura e Tecnologias

- **Backend:** Java 21, Spring Boot 3.3.5 (Spring Web, Spring Security, Spring Data JPA, Bean Validation)
- **Autenticação & Segurança:** Stateless JWT (RBAC por perfil) + Autenticação dedicada de hardware via cabeçalho `X-Device-Token`
- **Banco de Dados & Migrações:** PostgreSQL + Flyway (`V1` a `V6`)
- **Frontend:** HTML5, CSS3, JavaScript (ES6+ Vanilla) servido de forma integrada pelo Spring Boot
- **Build:** Apache Maven (`mvnw.bat` / `mvnw`)

---

## Módulos e Funcionalidades Implementadas

1. **Autenticação e Controle de Acesso (RBAC):**
   - Perfis com isolamento estrito de rotas e dados no servidor: `COORDENADOR`, `SUPERVISOR`, `RESPONSAVEL` e `ALUNO`.
2. **Gestão de Alunos, Matrículas e Vínculos Familiares:**
   - Cadastro completo de estudantes, emissão/ativação de crachá RFID, bloqueio preventivo de acesso e vínculo com responsáveis legais.
3. **Pessoas Autorizadas e Saídas Antecipadas:**
   - Cadastro, revogação e validade de terceiros autorizados para retirada.
   - Bloqueio automático de saídas antecipadas caso não exista autorização `VALIDA`, com trilha completa de auditoria.
4. **Controle de Acesso e Catracas IoT:**
   - Gestão de dispositivos de portaria com geração segura de token de dispositivo.
   - Validação em tempo real de `ENTRADA` e `SAIDA`, prevenção de duplicidade rápida (anti-passback) e simulador ao vivo no painel administrativo.
5. **Notificações Familiares e Central de Alertas:**
   - Disparo automático de notificações aos responsáveis a cada passagem autorizada ou negada na catraca.
   - Central de incidentes para Coordenação/Supervisão com classificação de severidade e registro de tratativa/resolução.
6. **Indicadores Gerenciais, Relatórios CSV e Portal do Aluno:**
   - Painel consolidado com ocupação escolar em tempo real (alunos presentes na escola no momento), movimentação diária e exportação de relatórios em **CSV (UTF-8 com BOM)**.
   - Quadro de comunicados institucionais e **Portal do Aluno** com dados do crachá e histórico individual de portaria.

---

## Como Executar Localmente

### Pré-requisitos
- **Java JDK 21** instalado
- **PostgreSQL** ativo com banco de dados configurado (ver `.env.example` / `application.properties`)

### Passos para iniciar (Windows / PowerShell)

1. Empacotar a aplicação:
   `.\mvnw.bat package -DskipTests`

2. Executar o servidor Spring Boot:
   `$javaPath = (.\mvnw.bat -v | Select-String "Java version").ToString() -replace ".*runtime: ", ""`
   `& "$javaPath\bin\java.exe" -jar target\school-security-system-1.0.0-SNAPSHOT.jar`

3. Acessar no navegador:
   Abra `http://localhost:8080`

---

## Credenciais de Demonstração (Dados Iniciais Flyway)

| Perfil | E-mail | Senha | Escopo de Acesso |
| :--- | :--- | :--- | :--- |
| **Coordenador** | `coordenador@escola.com` | `123456` | Gestão completa, relatórios CSV, alertas, catracas e comunicados |
| **Supervisor** | `supervisor@escola.com` | `123456` | Operação de portaria, saídas antecipadas, alertas e relatórios |
| **Responsável** | `responsavel@escola.com` | `123456` | Dependentes vinculados, notificações em tempo real e comunicados |
| **Aluno** | `aluno@escola.com` | `123456` | Portal do aluno, status da matrícula/crachá, histórico e comunicados |

---

## Estrutura do Projeto

- `src/main/java/com/escolasegura/core/` — Configuração de segurança, JWT, filtro de dispositivos e exceções globais
- `src/main/java/com/escolasegura/modules/auth/` — Login, emissão de tokens e perfis RBAC
- `src/main/java/com/escolasegura/modules/students/` — Cadastro de alunos e vínculos com responsáveis
- `src/main/java/com/escolasegura/modules/authorizations/` — Pessoas autorizadas, saídas antecipadas e auditoria
- `src/main/java/com/escolasegura/modules/accesscontrol/` — Dispositivos IoT, validação de catraca e logs de acesso
- `src/main/java/com/escolasegura/modules/notifications/` — Notificações para responsáveis e alertas de segurança
- `src/main/java/com/escolasegura/modules/reports/` — Indicadores gerenciais em tempo real e exportação CSV
- `src/main/java/com/escolasegura/modules/studentportal/` — Comunicados institucionais e portal do estudante
---

## Documentação de Engenharia de Software

- [PRD — Documento de Requisitos do Produto](docs/PRD.md)
- [ADR — Registros de Decisões Arquiteturais](docs/ADR.md)
- [SPECS — Especificações Técnicas e Contratos de API](docs/SPECS.md)

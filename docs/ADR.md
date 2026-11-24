# ADR — Architecture Decision Records
## Projeto: SSE (Sistema de Segurança Escolar)

## ADR-001: Arquitetura Monolítica Modular com Spring Boot 3 e Java 21
- **Status:** Aceito
- **Contexto:** Necessidade de transações ACID entre validação de catraca, notificações e alertas de segurança com deploy simplificado.
- **Decisão:** Monólito modular organizado por pacotes de domínio (`com.escolasegura.modules.*`) sobre Spring Boot 3.3.5, Java 21 e Spring Data JPA.

## ADR-002: Autenticação Híbrida (JWT Stateless e Token Dedicado para Dispositivos IoT)
- **Status:** Aceito
- **Contexto:** Usuários humanos operam via navegador com RBAC, enquanto catracas enviam eventos automatizados sem sessão.
- **Decisão:** Dois filtros independentes no Spring Security: `JwtAuthenticationFilter` (`Bearer <token>`) para usuários e `DeviceAuthenticationFilter` (`X-Device-Token` / `ROLE_DEVICE`) para hardware.

## ADR-003: Controle de Versão de Esquema com PostgreSQL e Flyway
- **Status:** Aceito
- **Contexto:** Evolução incremental em 6 fases exigindo integridade referencial para logs e auditoria.
- **Decisão:** PostgreSQL como banco relacional e migrations Flyway (`V1` a `V6`) como fonte única de verdade.

## ADR-004: Frontend Integrado em HTML5, CSS3 e JavaScript Vanilla (ES6+)
- **Status:** Aceito
- **Contexto:** Evitar complexidade de pipelines externos de frontend mantendo polling em tempo real e exportação CSV.
- **Decisão:** Servir interface estática diretamente em `src/main/resources/static` empacotada no `.jar` único.
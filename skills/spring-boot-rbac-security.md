# Skill: Segurança Stateless com JWT e RBAC no Spring Boot

## Objetivo
Proteger rotas REST por perfil de acesso (*Role-Based Access Control*) e autenticar dispositivos físicos de borda (catracas IoT) de forma isolada.

## Diretrizes Aplicadas
* **Autenticação Humana (JWT):** Emissão e validação de tokens Bearer contendo o perfil do usuário (`ADMIN`, `GUARDIAN`, `STUDENT`) via `JwtAuthenticationFilter`.
* **Autenticação de Hardware (IoT):** Validação de cabeçalho dedicado `X-Device-Key` via `DeviceApiKeyFilter` para leituras de crachá RFID em `/api/devices/**`.
* **Segregação de Rotas:** Isolamento estrito entre endpoints administrativos (`/api/admin/**`), familiares (`/api/guardian/**`) e estudantis (`/api/student/**`).
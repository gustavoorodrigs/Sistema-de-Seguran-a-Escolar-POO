# SPEC 01 — Autenticação, Autorização (RBAC) e Segurança IoT

**Módulo Java:** `com.school.security.auth`  
**Requisito Relacionado:** RF01, RNF03

## 1. Objetivo
Garantir que apenas usuários autenticados e dispositivos de portaria autorizados acessem os recursos da aplicação, aplicando controle de acesso baseado em papéis (*Role-Based Access Control*).

## 2. Atores
* **Usuário Web (`ADMIN`, `GUARDIAN`, `STUDENT`):** Autentica-se com login e senha e recebe um token JWT *stateless*.
* **Dispositivo IoT (`DEVICE`):** Catraca ou leitor RFID que envia requisições assinadas com o cabeçalho `X-Device-Key`.

## 3. Regras de Negócio
* Senhas nunca são armazenadas ou trafegadas em texto puro; o atributo `passwordHash` é encapsulado na entidade `User`.
* Tokens JWT expirados ou adulterados retornam HTTP `401 Unauthorized`.
* Tentativas de acesso a rotas administrativas (`/api/admin/**`) por perfis `STUDENT` ou `GUARDIAN` retornam HTTP `403 Forbidden`.
* Dispositivos com `active = false` têm suas chaves rejeitadas pelo `DeviceApiKeyFilter`.

## 4. Endpoints Principais
* `POST /api/auth/login` — Valida credenciais e retorna o token JWT e o perfil do usuário.
* `GET /api/auth/me` — Retorna os dados da sessão atual a partir do token enviado.

## 5. Classes POO Envolvidas
* `User`: Encapsula credenciais, verificação de papel (`isAdmin()`) e alteração segura de senha (`alterarSenha()`).
* `Device`: Encapsula o hash da chave de API e expõe `validarChave(String key)`.
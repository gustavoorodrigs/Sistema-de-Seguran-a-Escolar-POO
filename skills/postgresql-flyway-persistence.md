# Skill: Persistência Relacional e Versionamento de Schema (PostgreSQL + Flyway)

## Objetivo
Garantir integridade referencial, rastreabilidade de alterações no banco de dados e reprodutibilidade do ambiente de testes.

## Diretrizes Aplicadas
* **ORM / JPA:** Mapeamento objeto-relacional com Hibernate, chaves estrangeiras explícitas (`@ManyToOne`, `@OneToMany`) e restrições de unicidade (`badge_code`).
* **Versionamento Incremental:** Evolução controlada do schema em 6 scripts sequenciais (`V1` a `V6`) gerenciados pelo Flyway e consolidados no script único `database.sql`.
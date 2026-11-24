# Skill: Modelagem de Domínio Orientada a Objetos (Java / JPA)

## Objetivo
Evitar o antipadrão de entidades anêmicas em aplicações Spring Boot, garantindo a aplicação explícita dos 4 pilares de Programação Orientada a Objetos no núcleo do domínio.

## Diretrizes Aplicadas
* **Abstração:** Criação de superclasses abstratas (`Pessoa`, `RegistroAuditavel`) com `@MappedSuperclass` e interfaces de contrato comportamental (`Validavel`, `Notificavel`).
* **Herança:** Especialização de entidades concretas (`Student extends Pessoa`; `AccessLog`, `SecurityAlert` e `EarlyExit extends RegistroAuditavel`) reaproveitando atributos e ciclo de vida.
* **Encapsulamento:** Proteção de invariantes e transições de estado por meio de métodos de negócio dentro das próprias entidades (`Student.vincularCracha()`, `Student.bloquearCracha()`, `SecurityAlert.resolve()`, `EarlyExit.validarRegraNegocio()`).
* **Polimorfismo:** Sobrescrita de métodos (`@Override` em `resumirEvento()`, `gerarMensagemNotificacao()`, `getNivelSeveridade()`, `getIdentificacao()`) e processamento uniforme via coleções tipadas pelas interfaces em `AuditoriaPolimorficaService`.
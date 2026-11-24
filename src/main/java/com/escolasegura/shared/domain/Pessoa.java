package com.escolasegura.shared.domain;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name = "full_name", nullable = false)
    protected String fullName;

    @Column(name = "active", nullable = false)
    protected boolean active = true;

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isActive() {
        return active;
    }

    // Encapsulamento de mudança de estado
    public void ativar() {
        this.active = true;
    }

    public void desativar() {
        this.active = false;
    }

    // Contrato polimórfico obrigatório para subclasses
    public abstract String getIdentificacao();
}
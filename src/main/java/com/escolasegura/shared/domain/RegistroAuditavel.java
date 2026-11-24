package com.escolasegura.shared.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class RegistroAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    protected OffsetDateTime createdAt;

    @PrePersist
    protected void registrarDataCriacao() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public abstract String resumirEvento();
}
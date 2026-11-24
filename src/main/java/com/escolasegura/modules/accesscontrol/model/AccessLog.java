package com.escolasegura.modules.accesscontrol.model;

import com.escolasegura.modules.students.model.Student;
import com.escolasegura.shared.domain.Notificavel;
import com.escolasegura.shared.domain.RegistroAuditavel;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "access_logs")
public class AccessLog extends RegistroAuditavel implements Notificavel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false, length = 30)
    private ValidationStatus validationStatus;

    @Column(name = "denial_reason", length = 255)
    private String denialReason;

    @Column(name = "raw_identifier", nullable = false, length = 100)
    private String rawIdentifier;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private OffsetDateTime recordedAt;

    public AccessLog() {
    }

    public AccessLog(
            Student student,
            Device device,
            AccessDirection direction,
            ValidationStatus validationStatus,
            String denialReason,
            String rawIdentifier,
            OffsetDateTime recordedAt
    ) {
        this.student = student;
        this.device = device;
        this.direction = direction;
        this.validationStatus = validationStatus;
        this.denialReason = denialReason;
        this.rawIdentifier = rawIdentifier;
        this.recordedAt = recordedAt != null ? recordedAt : OffsetDateTime.now();
        this.createdAt = OffsetDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) recordedAt = OffsetDateTime.now();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    // --- Regras de Negócio e Polimorfismo (POO) ---

    public boolean isAutorizado() {
        if (this.validationStatus == null) {
            return false;
        }
        String status = this.validationStatus.name();
        return "AUTORIZADO".equals(status) || "GRANTED".equals(status) || "APPROVED".equals(status);
    }

    @Override
    public String resumirEvento() {
        String alunoNome = (this.student != null)
                ? this.student.getFullName()
                : "Desconhecido (" + this.rawIdentifier + ")";
        return String.format("[Catraca - %s] %s | Status: %s",
                this.direction, alunoNome, this.validationStatus);
    }

    @Override
    public String gerarMensagemNotificacao() {
        String alunoNome = (this.student != null)
                ? this.student.getFullName()
                : "Portador do crachá " + this.rawIdentifier;
        if (isAutorizado()) {
            return String.format("Movimentação registrada: %s realizou %s na unidade.",
                    alunoNome, this.direction);
        }
        return String.format("Tentativa bloqueada (%s) para %s. Motivo: %s",
                this.direction, alunoNome, this.denialReason);
    }

    @Override
    public String getNivelSeveridade() {
        return isAutorizado() ? "INFO" : "ALTA";
    }

    // --- Getters (getId e getCreatedAt vêm de RegistroAuditavel) ---

    public Student getStudent() { return student; }
    public Device getDevice() { return device; }
    public AccessDirection getDirection() { return direction; }
    public ValidationStatus getValidationStatus() { return validationStatus; }
    public String getDenialReason() { return denialReason; }
    public String getRawIdentifier() { return rawIdentifier; }
    public OffsetDateTime getRecordedAt() { return recordedAt; }
}
package com.escolasegura.modules.notifications.model;

import com.escolasegura.modules.accesscontrol.model.Device;
import com.escolasegura.modules.students.model.Student;
import com.escolasegura.shared.domain.Notificavel;
import com.escolasegura.shared.domain.RegistroAuditavel;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "security_alerts")
public class SecurityAlert extends RegistroAuditavel implements Notificavel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status = AlertStatus.ABERTO;

    @Column(name = "resolution_notes", length = 500)
    private String resolutionNotes;

    @Column(name = "resolved_by_email", length = 150)
    private String resolvedByEmail;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    protected SecurityAlert() {}

    public SecurityAlert(Student student, Device device, AlertSeverity severity, String category, String description) {
        this.student = student;
        this.device = device;
        this.severity = severity;
        this.category = category;
        this.description = description;
        this.status = AlertStatus.ABERTO;
        this.createdAt = OffsetDateTime.now();
    }

    // --- Regras de Negócio, Encapsulamento e Polimorfismo (POO) ---

    public void resolve(String notes, String operatorEmail) {
        if (this.status == AlertStatus.RESOLVIDO) {
            throw new IllegalStateException("Este alerta de segurança já foi encerrado anteriormente.");
        }
        if (notes == null || notes.isBlank()) {
            throw new IllegalArgumentException("As notas de resolução são obrigatórias para encerrar o alerta.");
        }
        this.status = AlertStatus.RESOLVIDO;
        this.resolutionNotes = notes.trim();
        this.resolvedByEmail = operatorEmail;
        this.resolvedAt = OffsetDateTime.now();
    }

    @Override
    public String resumirEvento() {
        return String.format("[Alerta %s] %s - %s (Status: %s)",
                this.severity, this.category, this.description, this.status);
    }

    @Override
    public String gerarMensagemNotificacao() {
        String nomeAluno = (this.student != null) ? this.student.getFullName() : "Não identificado";
        return String.format("ALERTA DE SEGURANÇA (%s): %s envolvendo %s.",
                this.severity, this.description, nomeAluno);
    }

    @Override
    public String getNivelSeveridade() {
        return (this.severity != null) ? this.severity.name() : "ALTA";
    }

    // --- Getters (getId e getCreatedAt vêm de RegistroAuditavel) ---

    public Student getStudent() { return student; }
    public Device getDevice() { return device; }
    public AlertSeverity getSeverity() { return severity; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public AlertStatus getStatus() { return status; }
    public String getResolutionNotes() { return resolutionNotes; }
    public String getResolvedByEmail() { return resolvedByEmail; }
    public OffsetDateTime getResolvedAt() { return resolvedAt; }
}
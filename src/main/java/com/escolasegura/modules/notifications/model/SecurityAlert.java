package com.escolasegura.modules.notifications.model;

import com.escolasegura.modules.accesscontrol.model.Device;
import com.escolasegura.modules.students.model.Student;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "security_alerts")
public class SecurityAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

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

    public void resolve(String notes, String operatorEmail) {
        this.status = AlertStatus.RESOLVIDO;
        this.resolutionNotes = notes;
        this.resolvedByEmail = operatorEmail;
        this.resolvedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public Device getDevice() { return device; }
    public AlertSeverity getSeverity() { return severity; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public AlertStatus getStatus() { return status; }
    public String getResolutionNotes() { return resolutionNotes; }
    public String getResolvedByEmail() { return resolvedByEmail; }
    public OffsetDateTime getResolvedAt() { return resolvedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
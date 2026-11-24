package com.escolasegura.modules.authorizations.model;

import com.escolasegura.modules.auth.model.User;
import com.escolasegura.modules.students.model.Student;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pickup_audit_logs")
public class PickupAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "authorized_pickup_id", nullable = false)
    private AuthorizedPickup authorizedPickup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private User changedByUser;

    @Column(nullable = false, length = 50)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private PickupStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private PickupStatus newStatus;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public PickupAuditLog() {
    }

    public PickupAuditLog(AuthorizedPickup authorizedPickup, Student student, User changedByUser, String action, PickupStatus previousStatus, PickupStatus newStatus, String reason) {
        this.authorizedPickup = authorizedPickup;
        this.student = student;
        this.changedByUser = changedByUser;
        this.action = action;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.createdAt = OffsetDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public AuthorizedPickup getAuthorizedPickup() {
        return authorizedPickup;
    }

    public Student getStudent() {
        return student;
    }

    public User getChangedByUser() {
        return changedByUser;
    }

    public String getAction() {
        return action;
    }

    public PickupStatus getPreviousStatus() {
        return previousStatus;
    }

    public PickupStatus getNewStatus() {
        return newStatus;
    }

    public String getReason() {
        return reason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}

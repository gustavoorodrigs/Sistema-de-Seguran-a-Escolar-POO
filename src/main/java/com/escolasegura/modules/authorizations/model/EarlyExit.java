package com.escolasegura.modules.authorizations.model;

import com.escolasegura.modules.auth.model.User;
import com.escolasegura.modules.students.model.Student;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "early_exits")
public class EarlyExit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "authorized_pickup_id", nullable = false)
    private AuthorizedPickup authorizedPickup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "authorized_by_user_id", nullable = false)
    private User authorizedByUser;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(name = "exit_time", nullable = false)
    private OffsetDateTime exitTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public EarlyExit() {
    }

    public EarlyExit(Student student, AuthorizedPickup authorizedPickup, User authorizedByUser, String reason, OffsetDateTime exitTime) {
        this.student = student;
        this.authorizedPickup = authorizedPickup;
        this.authorizedByUser = authorizedByUser;
        this.reason = reason;
        this.exitTime = exitTime != null ? exitTime : OffsetDateTime.now();
        this.createdAt = OffsetDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (exitTime == null) exitTime = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public AuthorizedPickup getAuthorizedPickup() {
        return authorizedPickup;
    }

    public User getAuthorizedByUser() {
        return authorizedByUser;
    }

    public String getReason() {
        return reason;
    }

    public OffsetDateTime getExitTime() {
        return exitTime;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}

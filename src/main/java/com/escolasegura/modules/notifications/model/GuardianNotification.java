package com.escolasegura.modules.notifications.model;

import com.escolasegura.modules.auth.model.User;
import com.escolasegura.modules.students.model.Student;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "guardian_notifications")
public class GuardianNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guardian_user_id", nullable = false)
    private User guardianUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "event_type", nullable = false, length = 40)
    private String eventType;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected GuardianNotification() {}

    public GuardianNotification(User guardianUser, Student student, String title, String message, String eventType) {
        this.guardianUser = guardianUser;
        this.student = student;
        this.title = title;
        this.message = message;
        this.eventType = eventType;
        this.read = false;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public User getGuardianUser() { return guardianUser; }
    public Student getStudent() { return student; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getEventType() { return eventType; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
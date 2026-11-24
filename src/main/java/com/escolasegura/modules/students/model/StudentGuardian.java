package com.escolasegura.modules.students.model;

import com.escolasegura.modules.auth.model.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "student_guardians", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_guardian", columnNames = {"student_id", "guardian_user_id"})
})
public class StudentGuardian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guardian_user_id", nullable = false)
    private User guardianUser;

    @Column(nullable = false, length = 50)
    private String relationship;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public StudentGuardian() {
    }

    public StudentGuardian(Student student, User guardianUser, String relationship) {
        this.student = student;
        this.guardianUser = guardianUser;
        this.relationship = relationship;
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

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public User getGuardianUser() {
        return guardianUser;
    }

    public void setGuardianUser(User guardianUser) {
        this.guardianUser = guardianUser;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}

package com.escolasegura.modules.studentportal.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "school_announcements")
public class SchoolAnnouncement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 40)
    private String category = "GERAL";

    @Column(name = "author_name", nullable = false, length = 150)
    private String authorName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public SchoolAnnouncement() {}

    public SchoolAnnouncement(String title, String content, String category, String authorName) {
        this.title = title;
        this.content = content;
        this.category = (category != null && !category.isBlank()) ? category.trim().toUpperCase() : "GERAL";
        this.authorName = authorName;
        this.createdAt = OffsetDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getCategory() { return category; }
    public String getAuthorName() { return authorName; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
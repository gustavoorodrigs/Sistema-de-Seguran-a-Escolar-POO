package com.escolasegura.modules.studentportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public class StudentPortalDtos {

    public record AnnouncementCreateRequest(
            @NotBlank(message = "O título do comunicado é obrigatório.")
            @Size(max = 150, message = "O título deve ter no máximo 150 caracteres.")
            String title,

            @NotBlank(message = "O conteúdo do comunicado é obrigatório.")
            String content,

            String category
    ) {}

    public record AnnouncementResponse(
            Long id,
            String title,
            String content,
            String category,
            String authorName,
            OffsetDateTime createdAt
    ) {}

    public record StudentAccessItem(
            OffsetDateTime recordedAt,
            String direction,
            String validationStatus,
            String deviceName,
            String denialReason
    ) {}

    public record StudentPortalResponse(
            String userName,
            String userEmail,
            boolean linkedStudentFound,
            Long studentId,
            String fullName,
            String className,
            LocalDate birthDate,
            String badgeCode,
            boolean active,
            List<StudentAccessItem> recentAccessLogs,
            List<AnnouncementResponse> announcements
    ) {}
}
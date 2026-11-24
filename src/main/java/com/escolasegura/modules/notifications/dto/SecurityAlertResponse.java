package com.escolasegura.modules.notifications.dto;

import com.escolasegura.modules.notifications.model.AlertSeverity;
import com.escolasegura.modules.notifications.model.AlertStatus;
import com.escolasegura.modules.notifications.model.SecurityAlert;
import java.time.OffsetDateTime;

public record SecurityAlertResponse(
        Long id,
        String studentName,
        String deviceName,
        AlertSeverity severity,
        String category,
        String description,
        AlertStatus status,
        String resolutionNotes,
        String resolvedByEmail,
        OffsetDateTime resolvedAt,
        OffsetDateTime createdAt
) {
    public static SecurityAlertResponse fromEntity(SecurityAlert a) {
        return new SecurityAlertResponse(
                a.getId(),
                a.getStudent() != null ? a.getStudent().getFullName() : "Não identificado",
                a.getDevice() != null ? a.getDevice().getName() : "-",
                a.getSeverity(),
                a.getCategory(),
                a.getDescription(),
                a.getStatus(),
                a.getResolutionNotes(),
                a.getResolvedByEmail(),
                a.getResolvedAt(),
                a.getCreatedAt()
        );
    }
}
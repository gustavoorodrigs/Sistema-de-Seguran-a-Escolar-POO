package com.escolasegura.modules.notifications.dto;

import com.escolasegura.modules.notifications.model.GuardianNotification;
import java.time.OffsetDateTime;

public record GuardianNotificationResponse(
        Long id,
        Long studentId,
        String studentName,
        String title,
        String message,
        String eventType,
        boolean read,
        OffsetDateTime createdAt
) {
    public static GuardianNotificationResponse fromEntity(GuardianNotification n) {
        return new GuardianNotificationResponse(
                n.getId(),
                n.getStudent().getId(),
                n.getStudent().getFullName(),
                n.getTitle(),
                n.getMessage(),
                n.getEventType(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
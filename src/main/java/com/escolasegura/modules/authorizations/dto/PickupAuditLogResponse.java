package com.escolasegura.modules.authorizations.dto;

import com.escolasegura.modules.authorizations.model.PickupAuditLog;
import com.escolasegura.modules.authorizations.model.PickupStatus;
import java.time.OffsetDateTime;

public record PickupAuditLogResponse(
        Long id,
        Long authorizedPickupId,
        String pickupPersonName,
        Long studentId,
        String studentName,
        String operatorName,
        String operatorEmail,
        String action,
        PickupStatus previousStatus,
        PickupStatus newStatus,
        String notes,
        OffsetDateTime createdAt
) {
    public static PickupAuditLogResponse fromEntity(PickupAuditLog log) {
        return new PickupAuditLogResponse(
                log.getId(),
                log.getAuthorizedPickup().getId(),
                log.getAuthorizedPickup().getFullName(),
                log.getStudent().getId(),
                log.getStudent().getFullName(),
                log.getChangedByUser().getName(),
                log.getChangedByUser().getEmail(),
                log.getAction(),
                log.getPreviousStatus(),
                log.getNewStatus(),
                log.getReason(),
                log.getCreatedAt()
        );
    }
}

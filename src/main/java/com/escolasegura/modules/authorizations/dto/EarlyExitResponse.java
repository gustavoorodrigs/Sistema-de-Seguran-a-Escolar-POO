package com.escolasegura.modules.authorizations.dto;

import com.escolasegura.modules.authorizations.model.EarlyExit;
import java.time.OffsetDateTime;

public record EarlyExitResponse(
        Long id,
        Long studentId,
        String studentName,
        String studentClassName,
        Long authorizedPickupId,
        String pickupPersonName,
        String pickupPersonRelationship,
        String authorizedByName,
        String reason,
        OffsetDateTime exitTime,
        OffsetDateTime createdAt
) {
    public static EarlyExitResponse fromEntity(EarlyExit ee) {
        return new EarlyExitResponse(
                ee.getId(),
                ee.getStudent().getId(),
                ee.getStudent().getFullName(),
                ee.getStudent().getClassName(),
                ee.getAuthorizedPickup().getId(),
                ee.getAuthorizedPickup().getFullName(),
                ee.getAuthorizedPickup().getRelationship(),
                ee.getAuthorizedByUser().getName(),
                ee.getReason(),
                ee.getExitTime(),
                ee.getCreatedAt()
        );
    }
}

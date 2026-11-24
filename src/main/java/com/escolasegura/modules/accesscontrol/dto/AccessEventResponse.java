package com.escolasegura.modules.accesscontrol.dto;

import com.escolasegura.modules.accesscontrol.model.AccessDirection;
import com.escolasegura.modules.accesscontrol.model.AccessLog;
import com.escolasegura.modules.accesscontrol.model.ValidationStatus;
import java.time.OffsetDateTime;

public record AccessEventResponse(
        Long eventId,
        Long studentId,
        String studentName,
        String className,
        String deviceName,
        AccessDirection direction,
        ValidationStatus status,
        String denialReason,
        OffsetDateTime recordedAt
) {
    public static AccessEventResponse fromEntity(AccessLog log) {
        return new AccessEventResponse(
                log.getId(),
                log.getStudent() != null ? log.getStudent().getId() : null,
                log.getStudent() != null ? log.getStudent().getFullName() : "Não Identificado",
                log.getStudent() != null ? log.getStudent().getClassName() : null,
                log.getDevice().getName(),
                log.getDirection(),
                log.getValidationStatus(),
                log.getDenialReason(),
                log.getRecordedAt()
        );
    }
}
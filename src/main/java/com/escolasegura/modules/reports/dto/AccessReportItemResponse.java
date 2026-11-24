package com.escolasegura.modules.reports.dto;

import java.time.OffsetDateTime;

public record AccessReportItemResponse(
        Long id,
        OffsetDateTime recordedAt,
        String studentName,
        String className,
        String badgeCode,
        String deviceName,
        String direction,
        String validationStatus,
        String denialReason
) {}

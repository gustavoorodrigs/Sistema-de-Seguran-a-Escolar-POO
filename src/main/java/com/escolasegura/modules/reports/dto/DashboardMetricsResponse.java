package com.escolasegura.modules.reports.dto;

public record DashboardMetricsResponse(
        long totalActiveStudents,
        long studentsCurrentlyInside,
        long todayAuthorizedEntries,
        long todayAuthorizedExits,
        long todayDeniedAttempts,
        long totalEarlyExits,
        long openSecurityAlerts
) {}
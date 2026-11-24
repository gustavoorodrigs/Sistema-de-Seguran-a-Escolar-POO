package com.escolasegura.modules.reports.controller;

import com.escolasegura.modules.reports.dto.AccessReportItemResponse;
import com.escolasegura.modules.reports.dto.DashboardMetricsResponse;
import com.escolasegura.modules.reports.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardMetricsResponse> getSummary() {
        return ResponseEntity.ok(reportService.getSummaryMetrics());
    }

    @GetMapping("/access-logs")
    public ResponseEntity<List<AccessReportItemResponse>> getAccessLogs(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String direction
    ) {
        return ResponseEntity.ok(reportService.filterAccessLogs(className, status, direction));
    }

    @GetMapping(value = "/access-logs/export.csv", produces = "text/csv; charset=UTF-8")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String direction
    ) {
        String csv = reportService.generateCsv(className, status, direction);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-acessos-escolares.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}
package com.escolasegura.modules.reports.service;

import com.escolasegura.modules.accesscontrol.model.AccessDirection;
import com.escolasegura.modules.accesscontrol.model.AccessLog;
import com.escolasegura.modules.accesscontrol.model.ValidationStatus;
import com.escolasegura.modules.accesscontrol.repository.AccessLogRepository;
import com.escolasegura.modules.authorizations.repository.EarlyExitRepository;
import com.escolasegura.modules.notifications.model.AlertStatus;
import com.escolasegura.modules.notifications.repository.SecurityAlertRepository;
import com.escolasegura.modules.reports.dto.AccessReportItemResponse;
import com.escolasegura.modules.reports.dto.DashboardMetricsResponse;
import com.escolasegura.modules.students.model.Student;
import com.escolasegura.modules.students.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportService {

    private final StudentRepository studentRepository;
    private final AccessLogRepository accessLogRepository;
    private final EarlyExitRepository earlyExitRepository;
    private final SecurityAlertRepository securityAlertRepository;

    public ReportService(
            StudentRepository studentRepository,
            AccessLogRepository accessLogRepository,
            EarlyExitRepository earlyExitRepository,
            SecurityAlertRepository securityAlertRepository
    ) {
        this.studentRepository = studentRepository;
        this.accessLogRepository = accessLogRepository;
        this.earlyExitRepository = earlyExitRepository;
        this.securityAlertRepository = securityAlertRepository;
    }

    @Transactional(readOnly = true)
    public DashboardMetricsResponse getSummaryMetrics() {
        List<Student> allStudents = studentRepository.findAll();
        long activeStudents = allStudents.stream().filter(Student::isActive).count();

        List<AccessLog> allLogs = accessLogRepository.findAll();
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        long todayEntries = 0;
        long todayExits = 0;
        long todayDenied = 0;

        Map<Long, AccessLog> latestAuthorizedByStudent = new HashMap<>();

        for (AccessLog log : allLogs) {
            boolean isToday = log.getRecordedAt() != null &&
                    log.getRecordedAt().atZoneSameInstant(ZoneId.systemDefault()).toLocalDate().equals(today);

            if (log.getValidationStatus() == ValidationStatus.AUTORIZADO) {
                if (isToday) {
                    if (log.getDirection() == AccessDirection.ENTRADA) todayEntries++;
                    else if (log.getDirection() == AccessDirection.SAIDA) todayExits++;
                }
                if (log.getStudent() != null) {
                    Long sid = log.getStudent().getId();
                    AccessLog current = latestAuthorizedByStudent.get(sid);
                    if (current == null || log.getRecordedAt().isAfter(current.getRecordedAt())) {
                        latestAuthorizedByStudent.put(sid, log);
                    }
                }
            } else if (isToday) {
                todayDenied++;
            }
        }

        long currentlyInside = latestAuthorizedByStudent.values().stream()
                .filter(l -> l.getDirection() == AccessDirection.ENTRADA)
                .count();

        long openAlerts = securityAlertRepository.countByStatus(AlertStatus.ABERTO);

        return new DashboardMetricsResponse(
                activeStudents,
                currentlyInside,
                todayEntries,
                todayExits,
                todayDenied,
                earlyExitRepository.count(),
                openAlerts
        );
    }

    @Transactional(readOnly = true)
    public List<AccessReportItemResponse> filterAccessLogs(String className, String status, String direction) {
        List<AccessLog> logs = accessLogRepository.findAll();
        logs.sort(Comparator.comparing(AccessLog::getRecordedAt).reversed());

        return logs.stream()
                .filter(l -> {
                    if (className != null && !className.isBlank()) {
                        String stuClass = l.getStudent() != null ? l.getStudent().getClassName() : "";
                        if (!stuClass.toLowerCase().contains(className.trim().toLowerCase())) return false;
                    }
                    if (status != null && !status.isBlank()) {
                        if (!l.getValidationStatus().name().equalsIgnoreCase(status.trim())) return false;
                    }
                    if (direction != null && !direction.isBlank()) {
                        if (!l.getDirection().name().equalsIgnoreCase(direction.trim())) return false;
                    }
                    return true;
                })
                .limit(200)
                .map(l -> new AccessReportItemResponse(
                        l.getId(),
                        l.getRecordedAt(),
                        l.getStudent() != null ? l.getStudent().getFullName() : "Não identificado",
                        l.getStudent() != null ? l.getStudent().getClassName() : "-",
                        l.getRawIdentifier(),
                        l.getDevice() != null ? l.getDevice().getName() : "-",
                        l.getDirection().name(),
                        l.getValidationStatus().name(),
                        l.getDenialReason() != null ? l.getDenialReason() : "Acesso regular"
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public String generateCsv(String className, String status, String direction) {
        List<AccessReportItemResponse> rows = filterAccessLogs(className, status, direction);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // BOM UTF-8 para Excel abrir acentos corretamente
        sb.append("ID;Data/Hora;Aluno;Turma;Cracha/RFID;Dispositivo;Direcao;Status;Observacao\n");
        for (AccessReportItemResponse r : rows) {
            String dt = r.recordedAt() != null
                    ? r.recordedAt().atZoneSameInstant(ZoneId.systemDefault()).format(fmt)
                    : "";
            sb.append(r.id()).append(';')
              .append(escapeCsv(dt)).append(';')
              .append(escapeCsv(r.studentName())).append(';')
              .append(escapeCsv(r.className())).append(';')
              .append(escapeCsv(r.badgeCode())).append(';')
              .append(escapeCsv(r.deviceName())).append(';')
              .append(escapeCsv(r.direction())).append(';')
              .append(escapeCsv(r.validationStatus())).append(';')
              .append(escapeCsv(r.denialReason())).append('\n');
        }
        return sb.toString();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        String clean = val.replace("\"", "\"\"");
        return "\"" + clean + "\"";
    }
}
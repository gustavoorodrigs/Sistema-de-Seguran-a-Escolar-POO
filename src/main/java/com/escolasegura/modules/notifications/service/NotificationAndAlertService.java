package com.escolasegura.modules.notifications.service;

import com.escolasegura.core.exception.ResourceNotFoundException;
import com.escolasegura.modules.accesscontrol.model.AccessDirection;
import com.escolasegura.modules.accesscontrol.model.Device;
import com.escolasegura.modules.notifications.dto.GuardianNotificationResponse;
import com.escolasegura.modules.notifications.dto.SecurityAlertResponse;
import com.escolasegura.modules.notifications.model.*;
import com.escolasegura.modules.notifications.repository.GuardianNotificationRepository;
import com.escolasegura.modules.notifications.repository.SecurityAlertRepository;
import com.escolasegura.modules.students.model.Student;
import com.escolasegura.modules.students.model.StudentGuardian;
import com.escolasegura.modules.students.repository.StudentGuardianRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationAndAlertService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final GuardianNotificationRepository notificationRepository;
    private final SecurityAlertRepository alertRepository;
    private final StudentGuardianRepository studentGuardianRepository;

    public NotificationAndAlertService(
            GuardianNotificationRepository notificationRepository,
            SecurityAlertRepository alertRepository,
            StudentGuardianRepository studentGuardianRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.alertRepository = alertRepository;
        this.studentGuardianRepository = studentGuardianRepository;
    }

    @Transactional
    public void notifyStudentAccess(Student student, Device device, AccessDirection direction, OffsetDateTime eventTime) {
        List<StudentGuardian> links = studentGuardianRepository.findByStudentId(student.getId());
        if (links.isEmpty()) return;

        String actionText = direction == AccessDirection.ENTRADA ? "entrou na escola" : "saiu da escola";
        String title = (direction == AccessDirection.ENTRADA ? "Entrada registrada: " : "Saída registrada: ") + student.getFullName();
        String timeStr = eventTime != null ? eventTime.format(TIME_FMT) : OffsetDateTime.now().format(TIME_FMT);
        String message = String.format("%s %s às %s pelo ponto %s (%s).",
                student.getFullName(), actionText, timeStr, device.getName(), device.getLocation());

        for (StudentGuardian link : links) {
            notificationRepository.save(new GuardianNotification(
                    link.getGuardianUser(),
                    student,
                    title,
                    message,
                    "ACESSO_" + direction.name()
            ));
        }
    }

    @Transactional
    public void notifyEarlyExit(Student student, String pickupPersonName, String relationship, String reason) {
        List<StudentGuardian> links = studentGuardianRepository.findByStudentId(student.getId());
        if (links.isEmpty()) return;

        String title = "Saída antecipada autorizada: " + student.getFullName();
        String message = String.format("Saída antecipada registrada para %s com %s (%s). Motivo: %s.",
                student.getFullName(), pickupPersonName, relationship, reason);

        for (StudentGuardian link : links) {
            notificationRepository.save(new GuardianNotification(
                    link.getGuardianUser(),
                    student,
                    title,
                    message,
                    "SAIDA_ANTECIPADA"
            ));
        }
    }

    @Transactional
    public void createSecurityAlert(Student student, Device device, AlertSeverity severity, String category, String description) {
        alertRepository.save(new SecurityAlert(student, device, severity, category, description));
    }

    @Transactional(readOnly = true)
    public List<GuardianNotificationResponse> listGuardianNotifications(String guardianEmail) {
        return notificationRepository.findTop30ByGuardianUserEmailOrderByCreatedAtDesc(guardianEmail)
                .stream()
                .map(GuardianNotificationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnreadForGuardian(String guardianEmail) {
        return notificationRepository.countByGuardianUserEmailAndReadFalse(guardianEmail);
    }

    @Transactional
    public void markAsRead(Long notificationId, String guardianEmail) {
        GuardianNotification notification = notificationRepository.findByIdAndGuardianUserEmail(notificationId, guardianEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String guardianEmail) {
        List<GuardianNotification> unread = notificationRepository.findByGuardianUserEmailAndReadFalse(guardianEmail);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional(readOnly = true)
    public List<SecurityAlertResponse> listSecurityAlerts() {
        return alertRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(SecurityAlertResponse::fromEntity)
                .toList();
    }

    @Transactional
    public SecurityAlertResponse resolveAlert(Long alertId, String resolutionNotes, String operatorEmail) {
        SecurityAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta de segurança não encontrado"));
        alert.resolve(resolutionNotes.trim(), operatorEmail);
        return SecurityAlertResponse.fromEntity(alertRepository.save(alert));
    }
}
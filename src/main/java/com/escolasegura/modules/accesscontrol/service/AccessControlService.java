package com.escolasegura.modules.accesscontrol.service;

import com.escolasegura.core.exception.BusinessRuleException;
import com.escolasegura.core.exception.ResourceNotFoundException;
import com.escolasegura.modules.accesscontrol.dto.*;
import com.escolasegura.modules.accesscontrol.model.*;
import com.escolasegura.modules.accesscontrol.repository.AccessLogRepository;
import com.escolasegura.modules.accesscontrol.repository.DeviceRepository;
import com.escolasegura.modules.notifications.model.AlertSeverity;
import com.escolasegura.modules.notifications.service.NotificationAndAlertService;
import com.escolasegura.modules.students.model.Student;
import com.escolasegura.modules.students.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AccessControlService {

    private static final int DUPLICATE_WINDOW_SECONDS = 30;

    private final DeviceRepository deviceRepository;
    private final AccessLogRepository accessLogRepository;
    private final StudentRepository studentRepository;
    private final DeviceTokenService deviceTokenService;
    private final NotificationAndAlertService notificationAndAlertService;

    public AccessControlService(
            DeviceRepository deviceRepository,
            AccessLogRepository accessLogRepository,
            StudentRepository studentRepository,
            DeviceTokenService deviceTokenService,
            NotificationAndAlertService notificationAndAlertService
    ) {
        this.deviceRepository = deviceRepository;
        this.accessLogRepository = accessLogRepository;
        this.studentRepository = studentRepository;
        this.deviceTokenService = deviceTokenService;
        this.notificationAndAlertService = notificationAndAlertService;
    }

    @Transactional
    public DeviceResponse registerDevice(DeviceCreateRequest request) {
        String rawToken = deviceTokenService.generateRawToken();
        String tokenHash = deviceTokenService.hashToken(rawToken);

        Device device = new Device(
                request.name().trim(),
                request.deviceType(),
                request.location().trim(),
                tokenHash,
                true
        );
        Device saved = deviceRepository.save(device);
        return DeviceResponse.fromEntity(saved, rawToken);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> listDevices() {
        return deviceRepository.findAll().stream()
                .map(d -> DeviceResponse.fromEntity(d, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AccessEventResponse> listRecentAccessLogs() {
        return accessLogRepository.findTop50ByOrderByRecordedAtDesc().stream()
                .map(AccessEventResponse::fromEntity)
                .toList();
    }

    @Transactional
    public DeviceResponse updateDeviceStatus(Long deviceId, boolean active) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo nÃ£o encontrado"));
        device.setActive(active);
        return DeviceResponse.fromEntity(deviceRepository.save(device), null);
    }

    @Transactional
    public AccessEventResponse processAccessEvent(Long authenticatedDeviceId, AccessEventRequest request) {
        Device device = deviceRepository.findById(authenticatedDeviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo authenticado nÃ£o encontrado"));

        if (!device.isActive()) {
            throw new BusinessRuleException("Dispositivo estÃ¡ inativo e nÃ£o pode registrar acessos");
        }

        String normalizedIdentifier = request.identifier().trim();
        OffsetDateTime eventTime = request.timestamp() != null ? request.timestamp() : OffsetDateTime.now();

        Optional<Student> studentOpt = studentRepository.findByBadgeCode(normalizedIdentifier);

        if (studentOpt.isEmpty()) {
            String reason = "Identificador/crachÃ¡ nÃ£o cadastrado no sistema";
            AccessLog deniedLog = new AccessLog(
                    null,
                    device,
                    request.direction(),
                    ValidationStatus.NEGADO,
                    reason,
                    normalizedIdentifier,
                    eventTime
            );
            notificationAndAlertService.createSecurityAlert(
                    null,
                    device,
                    AlertSeverity.ALTA,
                    "CRACHA_DESCONHECIDO",
                    "Tentativa de " + request.direction() + " com crachÃ¡ nÃ£o cadastrado (" + normalizedIdentifier + ") no ponto " + device.getName() + "."
            );
            return AccessEventResponse.fromEntity(accessLogRepository.save(deniedLog));
        }

        Student student = studentOpt.get();

        if (!student.isActive()) {
            String reason = "Cadastro do aluno encontra-se inativo";
            AccessLog deniedLog = new AccessLog(
                    student,
                    device,
                    request.direction(),
                    ValidationStatus.NEGADO,
                    reason,
                    normalizedIdentifier,
                    eventTime
            );
            notificationAndAlertService.createSecurityAlert(
                    student,
                    device,
                    AlertSeverity.ALTA,
                    "ALUNO_INATIVO",
                    "Tentativa de " + request.direction() + " bloqueada para aluno inativo (" + student.getFullName() + ") no ponto " + device.getName() + "."
            );
            return AccessEventResponse.fromEntity(accessLogRepository.save(deniedLog));
        }

        OffsetDateTime duplicateThreshold = eventTime.minusSeconds(DUPLICATE_WINDOW_SECONDS);
        Optional<AccessLog> recentDuplicate = accessLogRepository
                .findTopByStudentIdAndDirectionAndRecordedAtAfterOrderByRecordedAtDesc(
                        student.getId(),
                        request.direction(),
                        duplicateThreshold
                );

        if (recentDuplicate.isPresent()) {
            String reason = "Leitura duplicada detectada em intervalo inferior a " + DUPLICATE_WINDOW_SECONDS + "s";
            AccessLog duplicateLog = new AccessLog(
                    student,
                    device,
                    request.direction(),
                    ValidationStatus.NEGADO,
                    reason,
                    normalizedIdentifier,
                    eventTime
            );
            notificationAndAlertService.createSecurityAlert(
                    student,
                    device,
                    AlertSeverity.MEDIA,
                    "LEITURA_DUPLICADA",
                    "Leitura duplicada de " + request.direction() + " para " + student.getFullName() + " em menos de " + DUPLICATE_WINDOW_SECONDS + "s no ponto " + device.getName() + "."
            );
            return AccessEventResponse.fromEntity(accessLogRepository.save(duplicateLog));
        }

        AccessLog authorizedLog = new AccessLog(
                student,
                device,
                request.direction(),
                ValidationStatus.AUTORIZADO,
                null,
                normalizedIdentifier,
                eventTime
        );
        AccessLog savedLog = accessLogRepository.save(authorizedLog);

        notificationAndAlertService.notifyStudentAccess(student, device, request.direction(), eventTime);

        return AccessEventResponse.fromEntity(savedLog);
    }
}


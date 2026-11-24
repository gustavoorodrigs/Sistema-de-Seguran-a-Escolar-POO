package com.escolasegura.modules.accesscontrol.service;

import com.escolasegura.modules.accesscontrol.dto.AccessEventRequest;
import com.escolasegura.modules.accesscontrol.dto.AccessEventResponse;
import com.escolasegura.modules.accesscontrol.model.*;
import com.escolasegura.modules.accesscontrol.repository.AccessLogRepository;
import com.escolasegura.modules.accesscontrol.repository.DeviceRepository;
import com.escolasegura.modules.notifications.service.NotificationAndAlertService;
import com.escolasegura.modules.students.model.Student;
import com.escolasegura.modules.students.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessControlServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private AccessLogRepository accessLogRepository;

    @Mock
    private StudentRepository studentRepository;
    @Mock private NotificationAndAlertService notificationAndAlertService;

    private AccessControlService accessControlService;

    private Device activeDevice;
    private Student activeStudent;

    @BeforeEach
    void setUp() {
        accessControlService = new AccessControlService(
                deviceRepository,
                accessLogRepository,
                studentRepository,
                new DeviceTokenService(), notificationAndAlertService
        );

        activeDevice = new Device("Catraca Principal", DeviceType.CATRACA, "Portaria A", "hash123", true);
        activeDevice.setId(1L);

        activeStudent = new Student("Lucas Souza", LocalDate.of(2014, 5, 10), "5º Ano A", "82999999999", "RFID-1001", true);
        activeStudent.setId(10L);
    }

    @Test
    void shouldAuthorizeValidActiveStudentEntry() {
        OffsetDateTime now = OffsetDateTime.now();
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(activeDevice));
        when(studentRepository.findByBadgeCode("RFID-1001")).thenReturn(Optional.of(activeStudent));
        when(accessLogRepository.findTopByStudentIdAndDirectionAndRecordedAtAfterOrderByRecordedAtDesc(
                eq(10L), eq(AccessDirection.ENTRADA), any()
        )).thenReturn(Optional.empty());
        when(accessLogRepository.save(any(AccessLog.class))).thenAnswer(inv -> inv.getArgument(0));

        AccessEventResponse response = accessControlService.processAccessEvent(
                1L, new AccessEventRequest("RFID-1001", AccessDirection.ENTRADA, now)
        );

        assertEquals(ValidationStatus.AUTORIZADO, response.status());
        assertEquals("Lucas Souza", response.studentName());
        assertNull(response.denialReason());
    }

    @Test
    void shouldDenyInactiveStudentAccess() {
        activeStudent.setActive(false);
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(activeDevice));
        when(studentRepository.findByBadgeCode("RFID-1001")).thenReturn(Optional.of(activeStudent));
        when(accessLogRepository.save(any(AccessLog.class))).thenAnswer(inv -> inv.getArgument(0));

        AccessEventResponse response = accessControlService.processAccessEvent(
                1L, new AccessEventRequest("RFID-1001", AccessDirection.ENTRADA, OffsetDateTime.now())
        );

        assertEquals(ValidationStatus.NEGADO, response.status());
        assertTrue(response.denialReason().contains("inativo"));
    }

    @Test
    void shouldDenyUnknownBadgeIdentifier() {
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(activeDevice));
        when(studentRepository.findByBadgeCode("RFID-9999")).thenReturn(Optional.empty());
        when(accessLogRepository.save(any(AccessLog.class))).thenAnswer(inv -> inv.getArgument(0));

        AccessEventResponse response = accessControlService.processAccessEvent(
                1L, new AccessEventRequest("RFID-9999", AccessDirection.ENTRADA, OffsetDateTime.now())
        );

        assertEquals(ValidationStatus.NEGADO, response.status());
        assertEquals("Não Identificado", response.studentName());
    }

    @Test
    void shouldDenyDuplicateScanWithin30Seconds() {
        OffsetDateTime now = OffsetDateTime.now();
        AccessLog previousLog = new AccessLog(
                activeStudent, activeDevice, AccessDirection.ENTRADA, ValidationStatus.AUTORIZADO, null, "RFID-1001", now.minusSeconds(10)
        );

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(activeDevice));
        when(studentRepository.findByBadgeCode("RFID-1001")).thenReturn(Optional.of(activeStudent));
        when(accessLogRepository.findTopByStudentIdAndDirectionAndRecordedAtAfterOrderByRecordedAtDesc(
                eq(10L), eq(AccessDirection.ENTRADA), any()
        )).thenReturn(Optional.of(previousLog));
        when(accessLogRepository.save(any(AccessLog.class))).thenAnswer(inv -> inv.getArgument(0));

        AccessEventResponse response = accessControlService.processAccessEvent(
                1L, new AccessEventRequest("RFID-1001", AccessDirection.ENTRADA, now)
        );

        assertEquals(ValidationStatus.NEGADO, response.status());
        assertTrue(response.denialReason().contains("duplicada"));
    }
}
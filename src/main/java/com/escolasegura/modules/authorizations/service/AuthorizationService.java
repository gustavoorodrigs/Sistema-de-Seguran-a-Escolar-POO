package com.escolasegura.modules.authorizations.service;

import com.escolasegura.core.exception.AppException;
import com.escolasegura.core.exception.BusinessRuleException;
import com.escolasegura.core.exception.ResourceNotFoundException;
import com.escolasegura.modules.auth.model.User;
import com.escolasegura.modules.auth.repository.UserRepository;
import com.escolasegura.modules.authorizations.dto.*;
import com.escolasegura.modules.authorizations.model.AuthorizedPickup;
import com.escolasegura.modules.authorizations.model.EarlyExit;
import com.escolasegura.modules.authorizations.model.PickupAuditLog;
import com.escolasegura.modules.authorizations.model.PickupStatus;
import com.escolasegura.modules.authorizations.repository.AuthorizedPickupRepository;
import com.escolasegura.modules.authorizations.repository.EarlyExitRepository;
import com.escolasegura.modules.authorizations.repository.PickupAuditLogRepository;
import com.escolasegura.modules.notifications.service.NotificationAndAlertService;
import com.escolasegura.modules.students.model.Student;
import com.escolasegura.modules.students.repository.StudentGuardianRepository;
import com.escolasegura.modules.students.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AuthorizationService {

    private final AuthorizedPickupRepository authorizedPickupRepository;
    private final PickupAuditLogRepository pickupAuditLogRepository;
    private final EarlyExitRepository earlyExitRepository;
    private final StudentRepository studentRepository;
    private final StudentGuardianRepository studentGuardianRepository;
    private final UserRepository userRepository;
    private final NotificationAndAlertService notificationAndAlertService;

    public AuthorizationService(
            AuthorizedPickupRepository authorizedPickupRepository,
            PickupAuditLogRepository pickupAuditLogRepository,
            EarlyExitRepository earlyExitRepository,
            StudentRepository studentRepository,
            StudentGuardianRepository studentGuardianRepository,
            UserRepository userRepository,
            NotificationAndAlertService notificationAndAlertService
    ) {
        this.notificationAndAlertService = notificationAndAlertService;
        this.authorizedPickupRepository = authorizedPickupRepository;
        this.pickupAuditLogRepository = pickupAuditLogRepository;
        this.earlyExitRepository = earlyExitRepository;
        this.studentRepository = studentRepository;
        this.studentGuardianRepository = studentGuardianRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AuthorizedPickupResponse createAuthorizedPickup(AuthorizedPickupCreateRequest request, String operatorEmail) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não localizado com o ID: " + request.studentId()));

        User operator = userRepository.findByEmail(operatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Operador não localizado no sistema."));

        AuthorizedPickup pickup = new AuthorizedPickup(
                student,
                request.fullName().trim(),
                request.relationship().trim(),
                request.contactPhone().trim(),
                request.documentNumber() != null ? request.documentNumber().trim() : null,
                PickupStatus.VALIDA,
                request.validUntil()
        );

        AuthorizedPickup saved = authorizedPickupRepository.save(pickup);

        // Registro de auditoria inicial imutável (RF007 / RS004)
        PickupAuditLog auditLog = new PickupAuditLog(
                saved,
                student,
                operator,
                "CRIACAO",
                null,
                PickupStatus.VALIDA,
                "Cadastro inicial da pessoa autorizada para retirada."
        );
        pickupAuditLogRepository.save(auditLog);

        return AuthorizedPickupResponse.fromEntity(saved);
    }

    @Transactional
    public AuthorizedPickupResponse updatePickupStatus(Long pickupId, AuthorizedPickupStatusUpdateRequest request, String operatorEmail) {
        AuthorizedPickup pickup = authorizedPickupRepository.findById(pickupId)
                .orElseThrow(() -> new ResourceNotFoundException("Autorização não localizada com o ID: " + pickupId));

        User operator = userRepository.findByEmail(operatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Operador não localizado no sistema."));

        PickupStatus previousStatus = pickup.getStatus();
        PickupStatus newStatus = request.status();

        pickup.setStatus(newStatus);
        AuthorizedPickup updated = authorizedPickupRepository.save(pickup);

        // Trilha de auditoria obrigatória por exigência de segurança (RF007 / RS004)
        PickupAuditLog auditLog = new PickupAuditLog(
                updated,
                updated.getStudent(),
                operator,
                "ALTERACAO_STATUS",
                previousStatus,
                newStatus,
                request.reason().trim()
        );
        pickupAuditLogRepository.save(auditLog);

        return AuthorizedPickupResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public List<AuthorizedPickupResponse> listPickupsByStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Aluno não localizado com o ID: " + studentId);
        }

        return authorizedPickupRepository.findByStudentIdOrderByFullNameAsc(studentId)
                .stream()
                .map(AuthorizedPickupResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PickupAuditLogResponse> listAuditLogsByStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Aluno não localizado com o ID: " + studentId);
        }

        return pickupAuditLogRepository.findByStudentIdOrderByCreatedAtDesc(studentId)
                .stream()
                .map(PickupAuditLogResponse::fromEntity)
                .toList();
    }

    @Transactional
    public EarlyExitResponse registerEarlyExit(EarlyExitCreateRequest request, String operatorEmail) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não localizado com o ID: " + request.studentId()));

        if (!student.isActive()) {
            throw new BusinessRuleException("Não é permitido registrar saída antecipada para aluno com cadastro inativo.");
        }

        AuthorizedPickup pickup = authorizedPickupRepository.findById(request.authorizedPickupId())
                .orElseThrow(() -> new ResourceNotFoundException("Registro de autorização não localizado com o ID: " + request.authorizedPickupId()));

        if (!pickup.getStudent().getId().equals(student.getId())) {
            throw new BusinessRuleException("A pessoa informada não possui autorização vinculada a este aluno.");
        }

        // REGRA DE SEGURANÇA CRÍTICA [RS002]: Verificar status da autorização antes de liberar a retirada
        if (pickup.getStatus() != PickupStatus.VALIDA) {
            throw new BusinessRuleException(
                    "LIBERAÇÃO NEGADA (RS002): A autorização de '" + pickup.getFullName() +
                    "' está na situação " + pickup.getStatus().getDescricao().toUpperCase() +
                    ". A liberação só é permitida para pessoas com autorização VÁLIDA."
            );
        }

        User operator = userRepository.findByEmail(operatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Operador responsável pela liberação não localizado."));

        EarlyExit earlyExit = new EarlyExit(
                student,
                pickup,
                operator,
                request.reason().trim(),
                OffsetDateTime.now()
        );

        EarlyExit saved = earlyExitRepository.save(earlyExit);
        notificationAndAlertService.notifyEarlyExit(student, pickup.getFullName(), pickup.getRelationship(), request.reason().trim());
        return EarlyExitResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<EarlyExitResponse> listEarlyExits(Long studentId) {
        if (studentId != null) {
            return earlyExitRepository.findByStudentIdOrderByExitTimeDesc(studentId)
                    .stream()
                    .map(EarlyExitResponse::fromEntity)
                    .toList();
        }
        return earlyExitRepository.findAllByOrderByExitTimeDesc()
                .stream()
                .map(EarlyExitResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuthorizedPickupResponse> getPickupsForMyStudent(Long studentId, String guardianEmail) {
        // Validação estrita de escopo familiar (RS003)
        if (!studentGuardianRepository.isGuardianLinkedToStudent(studentId, guardianEmail)) {
            throw new AppException("Acesso negado: este aluno não pertence à sua conta familiar.", HttpStatus.FORBIDDEN);
        }

        return authorizedPickupRepository.findByStudentIdOrderByFullNameAsc(studentId)
                .stream()
                .map(AuthorizedPickupResponse::fromEntity)
                .toList();
    }
}

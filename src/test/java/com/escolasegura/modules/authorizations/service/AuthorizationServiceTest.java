package com.escolasegura.modules.authorizations.service;

import com.escolasegura.core.exception.BusinessRuleException;
import com.escolasegura.modules.auth.model.Role;
import com.escolasegura.modules.auth.model.User;
import com.escolasegura.modules.auth.repository.UserRepository;
import com.escolasegura.modules.authorizations.dto.AuthorizedPickupCreateRequest;
import com.escolasegura.modules.authorizations.dto.AuthorizedPickupStatusUpdateRequest;
import com.escolasegura.modules.authorizations.dto.EarlyExitCreateRequest;
import com.escolasegura.modules.authorizations.model.AuthorizedPickup;
import com.escolasegura.modules.authorizations.model.PickupStatus;
import com.escolasegura.modules.authorizations.repository.AuthorizedPickupRepository;
import com.escolasegura.modules.authorizations.repository.EarlyExitRepository;
import com.escolasegura.modules.authorizations.repository.PickupAuditLogRepository;
import com.escolasegura.modules.students.model.Student;
import com.escolasegura.modules.students.repository.StudentGuardianRepository;
import com.escolasegura.modules.students.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private AuthorizedPickupRepository authorizedPickupRepository;
    @Mock
    private PickupAuditLogRepository pickupAuditLogRepository;
    @Mock
    private EarlyExitRepository earlyExitRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StudentGuardianRepository studentGuardianRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    private Student activeStudent;
    private Student inactiveStudent;
    private User coordinator;
    private AuthorizedPickup validPickup;
    private AuthorizedPickup revokedPickup;

    @BeforeEach
    void setUp() {
        activeStudent = new Student("Lucas Souza", LocalDate.of(2014, 5, 14), "4º A", "(11) 98765-4321", "RFID-1001", true);
        activeStudent.setId(1L);

        inactiveStudent = new Student("Gabriel (Inativo)", LocalDate.of(2014, 2, 10), "4º A", "(11) 96543-2109", "RFID-1003", false);
        inactiveStudent.setId(3L);

        coordinator = new User("Ana Paula", "coordenador@escola.com", "hash", Role.COORDENADOR, true);
        coordinator.setId(1L);

        validPickup = new AuthorizedPickup(activeStudent, "Roberto Souza", "Tio", "(11) 98888-7777", "12.345.678-9", PickupStatus.VALIDA, LocalDate.now().plusMonths(6));
        validPickup.setId(10L);

        revokedPickup = new AuthorizedPickup(activeStudent, "Carlos (Ex-Van)", "Motorista", "(11) 97777-6666", "23.456.789-0", PickupStatus.REVOGADA, LocalDate.now().minusDays(10));
        revokedPickup.setId(11L);
    }

    @Test
    @DisplayName("Deve cadastrar pessoa autorizada com trilha de auditoria (RF005 / RF007)")
    void shouldCreateAuthorizedPickupWithAuditLog() {
        AuthorizedPickupCreateRequest request = new AuthorizedPickupCreateRequest(
                1L, "Roberto Souza", "Tio", "(11) 98888-7777", "12.345.678-9", LocalDate.now().plusMonths(6)
        );

        when(studentRepository.findById(1L)).thenReturn(Optional.of(activeStudent));
        when(userRepository.findByEmail("coordenador@escola.com")).thenReturn(Optional.of(coordinator));
        when(authorizedPickupRepository.save(any(AuthorizedPickup.class))).thenReturn(validPickup);

        var response = authorizationService.createAuthorizedPickup(request, "coordenador@escola.com");

        assertThat(response).isNotNull();
        assertThat(response.fullName()).isEqualTo("Roberto Souza");
        assertThat(response.status()).isEqualTo(PickupStatus.VALIDA);

        // Deve gravar log de auditoria imutável obrigatoriamente (RF007 / RS004)
        verify(pickupAuditLogRepository).save(any());
    }

    @Test
    @DisplayName("Deve gravar trilha de auditoria ao alterar status da autorização (RF007 / RS004)")
    void shouldWriteAuditLogOnStatusChange() {
        AuthorizedPickupStatusUpdateRequest request = new AuthorizedPickupStatusUpdateRequest(
                PickupStatus.REVOGADA,
                "Responsável solicitou cancelamento."
        );

        when(authorizedPickupRepository.findById(10L)).thenReturn(Optional.of(validPickup));
        when(userRepository.findByEmail("coordenador@escola.com")).thenReturn(Optional.of(coordinator));
        when(authorizedPickupRepository.save(any())).thenReturn(validPickup);

        authorizationService.updatePickupStatus(10L, request, "coordenador@escola.com");

        // Trilha de auditoria com status anterior e novo é obrigatória (RS004)
        verify(pickupAuditLogRepository).save(argThat(log ->
                log.getPreviousStatus() == PickupStatus.VALIDA &&
                log.getNewStatus() == PickupStatus.REVOGADA
        ));
    }

    @Test
    @DisplayName("Deve registrar saída antecipada com autorização válida (RF006 / RS002)")
    void shouldRegisterEarlyExitWithValidAuthorization() {
        EarlyExitCreateRequest request = new EarlyExitCreateRequest(1L, 10L, "Consulta médica agendada.");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(activeStudent));
        when(authorizedPickupRepository.findById(10L)).thenReturn(Optional.of(validPickup));
        when(userRepository.findByEmail("coordenador@escola.com")).thenReturn(Optional.of(coordinator));
        when(earlyExitRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = authorizationService.registerEarlyExit(request, "coordenador@escola.com");

        assertThat(response).isNotNull();
        assertThat(response.reason()).isEqualTo("Consulta médica agendada.");
    }

    @Test
    @DisplayName("Deve bloquear saída antecipada quando autorização estiver REVOGADA (RS002)")
    void shouldBlockEarlyExitWhenAuthorizationIsRevoked() {
        EarlyExitCreateRequest request = new EarlyExitCreateRequest(1L, 11L, "Tentativa de retirada não autorizada.");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(activeStudent));
        when(authorizedPickupRepository.findById(11L)).thenReturn(Optional.of(revokedPickup));

        assertThatThrownBy(() -> authorizationService.registerEarlyExit(request, "coordenador@escola.com"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("LIBERAÇÃO NEGADA");
    }

    @Test
    @DisplayName("Deve bloquear saída antecipada para aluno inativo (RF006)")
    void shouldBlockEarlyExitForInactiveStudent() {
        // O serviço verifica student.isActive() ANTES de consultar authorizedPickupRepository
        // → stub do pickup seria desnecessário; apenas o aluno é necessário aqui
        EarlyExitCreateRequest request = new EarlyExitCreateRequest(3L, 10L, "Tentativa indevida.");

        when(studentRepository.findById(3L)).thenReturn(Optional.of(inactiveStudent));

        assertThatThrownBy(() -> authorizationService.registerEarlyExit(request, "coordenador@escola.com"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cadastro inativo");
    }

    @Test
    @DisplayName("Deve bloquear responsável de visualizar autorizações de aluno não vinculado (RS003)")
    void shouldDenyGuardianAccessToUnlinkedStudentPickups() {
        when(studentGuardianRepository.isGuardianLinkedToStudent(99L, "invasor@escola.com")).thenReturn(false);

        assertThatThrownBy(() -> authorizationService.getPickupsForMyStudent(99L, "invasor@escola.com"))
                .hasMessageContaining("não pertence à sua conta familiar");
    }
}

package com.escolasegura.modules.students.service;

import com.escolasegura.core.exception.AppException;
import com.escolasegura.core.exception.BusinessRuleException;
import com.escolasegura.modules.auth.model.Role;
import com.escolasegura.modules.auth.model.User;
import com.escolasegura.modules.auth.repository.UserRepository;
import com.escolasegura.modules.students.dto.GuardianLinkRequest;
import com.escolasegura.modules.students.dto.StudentCreateRequest;
import com.escolasegura.modules.students.dto.StudentResponse;
import com.escolasegura.modules.students.model.Student;
import com.escolasegura.modules.students.model.StudentGuardian;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentGuardianRepository studentGuardianRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StudentService studentService;

    private Student student;
    private User guardianUser;

    @BeforeEach
    void setUp() {
        student = new Student("Lucas Souza", LocalDate.of(2014, 5, 14), "4º A", "(11) 98765-4321", "RFID-1001", true);
        student.setId(1L);

        guardianUser = new User("Patrícia Souza", "responsavel@escola.com", "hash", Role.RESPONSAVEL, true);
        guardianUser.setId(10L);
    }

    @Test
    @DisplayName("Deve cadastrar aluno com sucesso quando o crachá for inédito")
    void shouldCreateStudentSuccessfully() {
        StudentCreateRequest request = new StudentCreateRequest(
                "Lucas Souza",
                LocalDate.of(2014, 5, 14),
                "4º A",
                "(11) 98765-4321",
                "RFID-1001",
                true
        );

        when(studentRepository.existsByBadgeCode("RFID-1001")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        StudentResponse response = studentService.createStudent(request);

        assertThat(response).isNotNull();
        assertThat(response.fullName()).isEqualTo("Lucas Souza");
        assertThat(response.badgeCode()).isEqualTo("RFID-1001");
        assertThat(response.active()).isTrue();
    }

    @Test
    @DisplayName("Deve impedir cadastro quando crachá já estiver em uso")
    void shouldFailWhenBadgeCodeAlreadyExists() {
        StudentCreateRequest request = new StudentCreateRequest(
                "Outro Aluno",
                LocalDate.of(2014, 5, 14),
                "4º A",
                "(11) 98765-4321",
                "RFID-1001",
                true
        );

        when(studentRepository.existsByBadgeCode("RFID-1001")).thenReturn(true);

        assertThatThrownBy(() -> studentService.createStudent(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("crachá/código de acesso");
    }

    @Test
    @DisplayName("Deve inativar aluno sem exclusão física do registro (RF002)")
    void shouldDeactivateStudentWithoutPhysicalDeletion() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);

        StudentResponse response = studentService.updateStudentStatus(1L, false);

        assertThat(response.active()).isFalse();
        verify(studentRepository).save(student);
    }

    @Test
    @DisplayName("Deve vincular responsável familiar com perfil RESPONSAVEL ao aluno")
    void shouldLinkGuardianSuccessfully() {
        GuardianLinkRequest request = new GuardianLinkRequest(10L, "Mãe");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.findById(10L)).thenReturn(Optional.of(guardianUser));
        when(studentGuardianRepository.existsByStudentIdAndGuardianUserId(1L, 10L)).thenReturn(false);

        StudentResponse response = studentService.linkGuardian(1L, request);

        assertThat(response).isNotNull();
        verify(studentGuardianRepository).save(any(StudentGuardian.class));
    }

    @Test
    @DisplayName("Deve barrar no servidor consulta de aluno que não pertence ao responsável autenticado (RS003)")
    void shouldDenyAccessToNonLinkedStudent() {
        when(studentGuardianRepository.isGuardianLinkedToStudent(99L, "invasor@escola.com")).thenReturn(false);

        assertThatThrownBy(() -> studentService.getStudentForGuardianById(99L, "invasor@escola.com"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("não está vinculado ao seu perfil");
    }
}

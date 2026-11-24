package com.escolasegura.modules.students.service;

import com.escolasegura.core.exception.AppException;
import com.escolasegura.core.exception.BusinessRuleException;
import com.escolasegura.core.exception.ResourceNotFoundException;
import com.escolasegura.modules.auth.model.Role;
import com.escolasegura.modules.auth.model.User;
import com.escolasegura.modules.auth.repository.UserRepository;
import com.escolasegura.modules.students.dto.*;
import com.escolasegura.modules.students.model.Student;
import com.escolasegura.modules.students.model.StudentGuardian;
import com.escolasegura.modules.students.repository.StudentGuardianRepository;
import com.escolasegura.modules.students.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentGuardianRepository studentGuardianRepository;
    private final UserRepository userRepository;

    public StudentService(
            StudentRepository studentRepository,
            StudentGuardianRepository studentGuardianRepository,
            UserRepository userRepository
    ) {
        this.studentRepository = studentRepository;
        this.studentGuardianRepository = studentGuardianRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public StudentResponse createStudent(StudentCreateRequest request) {
        String badge = request.badgeCode().trim();
        if (studentRepository.existsByBadgeCode(badge)) {
            throw new BusinessRuleException("Já existe um aluno cadastrado com o crachá/código de acesso: " + badge);
        }

        Student student = new Student(
                request.fullName().trim(),
                request.birthDate(),
                request.className().trim(),
                request.contactPhone().trim(),
                badge,
                request.isActiveOrTrue()
        );

        Student saved = studentRepository.save(student);
        return StudentResponse.fromEntity(saved);
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentUpdateRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não localizado com o ID: " + id));

        String badge = request.badgeCode().trim();
        if (studentRepository.existsByBadgeCodeAndIdNot(badge, id)) {
            throw new BusinessRuleException("O crachá/código de acesso " + badge + " já está em uso por outro aluno.");
        }

        student.setFullName(request.fullName().trim());
        student.setBirthDate(request.birthDate());
        student.setClassName(request.className().trim());
        student.setContactPhone(request.contactPhone().trim());
        student.setBadgeCode(badge);
        student.setActive(request.active());

        Student updated = studentRepository.save(student);
        return StudentResponse.fromEntity(updated);
    }

    @Transactional
    public StudentResponse updateStudentStatus(Long id, boolean active) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não localizado com o ID: " + id));

        student.setActive(active);
        Student updated = studentRepository.save(student);
        return StudentResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não localizado com o ID: " + id));
        return StudentResponse.fromEntity(student);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> searchStudents(String name, String className, Boolean active) {
        String searchName = (name != null && !name.isBlank()) ? name.trim() : null;
        String searchClass = (className != null && !className.isBlank()) ? className.trim() : null;

        return studentRepository.searchStudents(searchName, searchClass, active)
                .stream()
                .map(StudentResponse::fromEntity)
                .toList();
    }

    @Transactional
    public StudentResponse linkGuardian(Long studentId, GuardianLinkRequest request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não localizado com o ID: " + studentId));

        User guardianUser = userRepository.findById(request.guardianUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário responsável não localizado com o ID: " + request.guardianUserId()));

        if (guardianUser.getRole() != Role.RESPONSAVEL) {
            throw new BusinessRuleException("O usuário informado não possui perfil de Responsável.");
        }

        if (studentGuardianRepository.existsByStudentIdAndGuardianUserId(studentId, guardianUser.getId())) {
            throw new BusinessRuleException("Este responsável já está vinculado ao aluno.");
        }

        StudentGuardian link = new StudentGuardian(student, guardianUser, request.relationship().trim());
        studentGuardianRepository.save(link);

        Student reloaded = studentRepository.findById(studentId).orElseThrow();
        return StudentResponse.fromEntity(reloaded);
    }

    @Transactional
    public StudentResponse unlinkGuardian(Long studentId, Long guardianUserId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não localizado com o ID: " + studentId));

        StudentGuardian link = studentGuardianRepository.findByStudentIdAndGuardianUserId(studentId, guardianUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Vínculo não localizado entre o aluno e o responsável informado."));

        studentGuardianRepository.delete(link);

        Student reloaded = studentRepository.findById(studentId).orElseThrow();
        return StudentResponse.fromEntity(reloaded);
    }

    @Transactional(readOnly = true)
    public List<GuardianStudentResponse> getStudentsForGuardian(String guardianEmail) {
        return studentGuardianRepository.findStudentsByGuardianEmail(guardianEmail)
                .stream()
                .map(GuardianStudentResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public GuardianStudentResponse getStudentForGuardianById(Long studentId, String guardianEmail) {
        if (!studentGuardianRepository.isGuardianLinkedToStudent(studentId, guardianEmail)) {
            throw new AppException("Acesso negado: este aluno não está vinculado ao seu perfil.", HttpStatus.FORBIDDEN);
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não localizado com o ID: " + studentId));

        return GuardianStudentResponse.fromEntity(student);
    }
}

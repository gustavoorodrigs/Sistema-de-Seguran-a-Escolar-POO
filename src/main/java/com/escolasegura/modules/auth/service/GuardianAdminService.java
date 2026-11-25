package com.escolasegura.modules.auth.service;

import com.escolasegura.core.exception.BusinessRuleException;
import com.escolasegura.core.exception.ResourceNotFoundException;
import com.escolasegura.modules.auth.dto.GuardianAccountResponse;
import com.escolasegura.modules.auth.dto.GuardianCreateRequest;
import com.escolasegura.modules.auth.model.Role;
import com.escolasegura.modules.auth.model.User;
import com.escolasegura.modules.auth.repository.UserRepository;
import com.escolasegura.modules.students.model.Student;
import com.escolasegura.modules.students.model.StudentGuardian;
import com.escolasegura.modules.students.repository.StudentGuardianRepository;
import com.escolasegura.modules.students.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GuardianAdminService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentGuardianRepository studentGuardianRepository;
    private final PasswordEncoder passwordEncoder;

    public GuardianAdminService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            StudentGuardianRepository studentGuardianRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.studentGuardianRepository = studentGuardianRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public GuardianAccountResponse createGuardian(GuardianCreateRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessRuleException("Já existe um usuário cadastrado com o e-mail: " + normalizedEmail);
        }

        Student studentToLink = null;
        if (request.studentId() != null) {
            if (request.relationship() == null || request.relationship().isBlank()) {
                throw new BusinessRuleException("Informe o grau de parentesco ao selecionar um aluno para vínculo.");
            }
            studentToLink = studentRepository.findById(request.studentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Aluno não localizado com o ID: " + request.studentId()));
        }

        User guardian = new User(
                request.name().trim(),
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                Role.RESPONSAVEL,
                true
        );
        User savedGuardian = userRepository.save(guardian);

        if (studentToLink != null) {
            StudentGuardian link = new StudentGuardian(studentToLink, savedGuardian, request.relationship().trim());
            studentGuardianRepository.save(link);
        }

        List<StudentGuardian> links = studentGuardianRepository.findByGuardianUserId(savedGuardian.getId());
        return GuardianAccountResponse.fromEntity(savedGuardian, links);
    }

    @Transactional(readOnly = true)
    public List<GuardianAccountResponse> listGuardians() {
        return userRepository.findByRoleOrderByNameAsc(Role.RESPONSAVEL)
                .stream()
                .map(user -> GuardianAccountResponse.fromEntity(
                        user,
                        studentGuardianRepository.findByGuardianUserId(user.getId())
                ))
                .toList();
    }

    @Transactional
    public GuardianAccountResponse updateGuardianStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Responsável não localizado com o ID: " + id));

        if (user.getRole() != Role.RESPONSAVEL) {
            throw new BusinessRuleException("O usuário informado não possui perfil de Responsável.");
        }

        user.setActive(active);
        User updated = userRepository.save(user);
        List<StudentGuardian> links = studentGuardianRepository.findByGuardianUserId(updated.getId());
        return GuardianAccountResponse.fromEntity(updated, links);
    }
}
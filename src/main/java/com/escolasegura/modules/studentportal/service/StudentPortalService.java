package com.escolasegura.modules.studentportal.service;

import com.escolasegura.modules.accesscontrol.model.AccessLog;
import com.escolasegura.modules.accesscontrol.repository.AccessLogRepository;
import com.escolasegura.modules.auth.model.User;
import com.escolasegura.modules.auth.repository.UserRepository;
import com.escolasegura.modules.studentportal.dto.StudentPortalDtos.*;
import com.escolasegura.modules.studentportal.model.SchoolAnnouncement;
import com.escolasegura.modules.studentportal.repository.SchoolAnnouncementRepository;
import com.escolasegura.modules.students.model.Student;
import com.escolasegura.modules.students.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class StudentPortalService {

    private final SchoolAnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final AccessLogRepository accessLogRepository;

    public StudentPortalService(
            SchoolAnnouncementRepository announcementRepository,
            UserRepository userRepository,
            StudentRepository studentRepository,
            AccessLogRepository accessLogRepository
    ) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.accessLogRepository = accessLogRepository;
    }

    @Transactional
    public AnnouncementResponse createAnnouncement(AnnouncementCreateRequest req, String authorEmail) {
        String authorName = userRepository.findByEmail(authorEmail)
                .map(User::getName)
                .orElse("Coordenação Escolar");

        SchoolAnnouncement saved = announcementRepository.save(
                new SchoolAnnouncement(req.title().trim(), req.content().trim(), req.category(), authorName)
        );
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> listAnnouncements() {
        return announcementRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentPortalResponse getStudentDashboard(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado."));

        List<AnnouncementResponse> announcements = listAnnouncements();

        // Vincula o usuário ALUNO ao seu registro em students pelo nome completo (ou primeiro aluno ativo caso seja conta demo)
        List<Student> allStudents = studentRepository.findAll();
        String normalizedUser = user.getName().toLowerCase().replace("aluno", "").trim();

        Optional<Student> matched = allStudents.stream()
                .filter(s -> s.getFullName().equalsIgnoreCase(user.getName())
                        || (!normalizedUser.isBlank() && s.getFullName().toLowerCase().contains(normalizedUser)))
                .findFirst();

        if (matched.isEmpty() && !allStudents.isEmpty()) {
            matched = Optional.of(allStudents.get(0));
        }

        if (matched.isEmpty()) {
            return new StudentPortalResponse(
                    user.getName(), user.getEmail(), false,
                    null, user.getName(), "Não vinculada", null, "-", user.isActive(),
                    List.of(), announcements
            );
        }

        Student student = matched.get();
        List<StudentAccessItem> logs = accessLogRepository.findAll().stream()
                .filter(l -> l.getStudent() != null && l.getStudent().getId().equals(student.getId()))
                .sorted(Comparator.comparing(AccessLog::getRecordedAt).reversed())
                .limit(30)
                .map(l -> new StudentAccessItem(
                        l.getRecordedAt(),
                        l.getDirection().name(),
                        l.getValidationStatus().name(),
                        l.getDevice() != null ? l.getDevice().getName() : "-",
                        l.getDenialReason() != null ? l.getDenialReason() : "Acesso regular"
                ))
                .toList();

        return new StudentPortalResponse(
                user.getName(),
                user.getEmail(),
                true,
                student.getId(),
                student.getFullName(),
                student.getClassName(),
                student.getBirthDate(),
                student.getBadgeCode(),
                student.isActive(),
                logs,
                announcements
        );
    }

    private AnnouncementResponse toResponse(SchoolAnnouncement a) {
        return new AnnouncementResponse(
                a.getId(),
                a.getTitle(),
                a.getContent(),
                a.getCategory(),
                a.getAuthorName(),
                a.getCreatedAt()
        );
    }
}
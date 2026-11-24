package com.escolasegura.core.config;

import com.escolasegura.modules.auth.model.Role;
import com.escolasegura.modules.auth.model.User;
import com.escolasegura.modules.auth.repository.UserRepository;
import com.escolasegura.modules.authorizations.model.AuthorizedPickup;
import com.escolasegura.modules.authorizations.model.EarlyExit;
import com.escolasegura.modules.authorizations.model.PickupAuditLog;
import com.escolasegura.modules.authorizations.model.PickupStatus;
import com.escolasegura.modules.authorizations.repository.AuthorizedPickupRepository;
import com.escolasegura.modules.authorizations.repository.EarlyExitRepository;
import com.escolasegura.modules.authorizations.repository.PickupAuditLogRepository;
import com.escolasegura.modules.students.model.Student;
import com.escolasegura.modules.students.model.StudentGuardian;
import com.escolasegura.modules.students.repository.StudentGuardianRepository;
import com.escolasegura.modules.students.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;
    private final StudentGuardianRepository studentGuardianRepository;
    private final AuthorizedPickupRepository authorizedPickupRepository;
    private final PickupAuditLogRepository pickupAuditLogRepository;
    private final EarlyExitRepository earlyExitRepository;

    public DataInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            StudentRepository studentRepository,
            StudentGuardianRepository studentGuardianRepository,
            AuthorizedPickupRepository authorizedPickupRepository,
            PickupAuditLogRepository pickupAuditLogRepository,
            EarlyExitRepository earlyExitRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.studentRepository = studentRepository;
        this.studentGuardianRepository = studentGuardianRepository;
        this.authorizedPickupRepository = authorizedPickupRepository;
        this.pickupAuditLogRepository = pickupAuditLogRepository;
        this.earlyExitRepository = earlyExitRepository;
    }

    @Override
    public void run(String... args) {
        User coord = seedUserIfNotExists("Coordenadora Ana Paula", "coordenador@escola.com", "Senha@123", Role.COORDENADOR, true);
        User sup = seedUserIfNotExists("Supervisor Marcos Silva", "supervisor@escola.com", "Senha@123", Role.SUPERVISOR, true);
        User resp = seedUserIfNotExists("Responsável Patrícia Souza", "responsavel@escola.com", "Senha@123", Role.RESPONSAVEL, true);
        User alunoUser = seedUserIfNotExists("Aluno Lucas Souza", "aluno@escola.com", "Senha@123", Role.ALUNO, true);
        seedUserIfNotExists("Usuário Desativado", "inativo@escola.com", "Senha@123", Role.RESPONSAVEL, false);

        Student s1 = seedStudentsAndGuardians(resp);

        if (s1 != null && authorizedPickupRepository.findByStudentIdOrderByFullNameAsc(s1.getId()).isEmpty()) {
            // Pessoa autorizada válida
            AuthorizedPickup p1 = new AuthorizedPickup(
                    s1,
                    "Roberto Souza",
                    "Tio",
                    "(11) 98888-7777",
                    "12.345.678-9",
                    PickupStatus.VALIDA,
                    LocalDate.now().plusMonths(6)
            );
            AuthorizedPickup savedP1 = authorizedPickupRepository.save(p1);
            pickupAuditLogRepository.save(new PickupAuditLog(
                    savedP1, s1, coord, "CRIACAO", null, PickupStatus.VALIDA, "Cadastro inicial autorizado pela coordenação."
            ));

            // Pessoa com autorização revogada (demonstração RS002)
            AuthorizedPickup p2 = new AuthorizedPickup(
                    s1,
                    "Carlos Transporte (Ex-Van)",
                    "Motorista Escolar",
                    "(11) 97777-6666",
                    "23.456.789-0",
                    PickupStatus.REVOGADA,
                    LocalDate.now().minusDays(10)
            );
            AuthorizedPickup savedP2 = authorizedPickupRepository.save(p2);
            pickupAuditLogRepository.save(new PickupAuditLog(
                    savedP2, s1, coord, "REVOGACAO", PickupStatus.VALIDA, PickupStatus.REVOGADA, "Responsável familiar solicitou cancelamento do contrato com a van escolar."
            ));

            // Saída antecipada prévia com sucesso
            earlyExitRepository.save(new EarlyExit(
                    s1, savedP1, coord, "Consulta médica / odontológica agendada", OffsetDateTime.now().minusHours(3)
            ));

            log.info("Pessoas autorizadas e auditorias iniciais criadas para o aluno: {}", s1.getFullName());
        }
    }

    private User seedUserIfNotExists(String name, String email, String rawPassword, Role role, boolean active) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User(name, email, passwordEncoder.encode(rawPassword), role, active);
            User saved = userRepository.save(user);
            log.info("Usuário inicial criado: {} ({}) - Ativo: {}", email, role, active);
            return saved;
        });
    }

    private Student seedStudentsAndGuardians(User guardian) {
        Student mainStudent = null;
        if (!studentRepository.existsByBadgeCode("RFID-1001")) {
            Student s1 = new Student("Lucas Souza", LocalDate.of(2014, 5, 14), "4º A", "(11) 98765-4321", "RFID-1001", true);
            mainStudent = studentRepository.save(s1);
            studentGuardianRepository.save(new StudentGuardian(s1, guardian, "Mãe"));
            log.info("Aluno inicial criado: {} (RFID-1001)", s1.getFullName());
        } else {
            mainStudent = studentRepository.findByBadgeCode("RFID-1001").orElse(null);
        }

        if (!studentRepository.existsByBadgeCode("RFID-1002")) {
            Student s2 = new Student("Mariana Oliveira", LocalDate.of(2013, 9, 20), "5º B", "(11) 97654-3210", "RFID-1002", true);
            studentRepository.save(s2);
            studentGuardianRepository.save(new StudentGuardian(s2, guardian, "Tia / Tutora"));
            log.info("Aluno inicial criado: {} (RFID-1002)", s2.getFullName());
        }

        if (!studentRepository.existsByBadgeCode("RFID-1003")) {
            Student s3 = new Student("Gabriel Santos (Inativo)", LocalDate.of(2014, 2, 10), "4º A", "(11) 96543-2109", "RFID-1003", false);
            studentRepository.save(s3);
            log.info("Aluno inicial criado: {} (RFID-1003) - Inativo", s3.getFullName());
        }

        return mainStudent;
    }
}

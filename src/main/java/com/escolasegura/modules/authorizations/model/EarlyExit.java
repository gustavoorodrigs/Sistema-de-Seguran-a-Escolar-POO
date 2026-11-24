package com.escolasegura.modules.authorizations.model;

import com.escolasegura.modules.auth.model.User;
import com.escolasegura.modules.students.model.Student;
import com.escolasegura.shared.domain.RegistroAuditavel;
import com.escolasegura.shared.domain.Validavel;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "early_exits")
public class EarlyExit extends RegistroAuditavel implements Validavel {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "authorized_pickup_id", nullable = false)
    private AuthorizedPickup authorizedPickup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "authorized_by_user_id", nullable = false)
    private User authorizedByUser;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(name = "exit_time", nullable = false)
    private OffsetDateTime exitTime;

    public EarlyExit() {
    }

    public EarlyExit(Student student, AuthorizedPickup authorizedPickup, User authorizedByUser, String reason, OffsetDateTime exitTime) {
        this.student = student;
        this.authorizedPickup = authorizedPickup;
        this.authorizedByUser = authorizedByUser;
        this.reason = reason;
        this.exitTime = exitTime != null ? exitTime : OffsetDateTime.now();
        this.createdAt = OffsetDateTime.now();
        validarRegraNegocio();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (exitTime == null) exitTime = OffsetDateTime.now();
    }

    // --- Regras de Negócio e Polimorfismo (POO) ---

    @Override
    public void validarRegraNegocio() {
        if (this.student == null || !this.student.isActive()) {
            throw new IllegalStateException("Não é possível autorizar saída antecipada para um aluno inativo.");
        }
        if (this.authorizedPickup == null) {
            throw new IllegalArgumentException("É obrigatório informar o responsável autorizado para a retirada.");
        }
        if (this.authorizedByUser == null) {
            throw new IllegalArgumentException("É obrigatório registrar o usuário que autorizou a saída.");
        }
        if (this.reason == null || this.reason.isBlank()) {
            throw new IllegalArgumentException("O motivo da saída antecipada é obrigatório.");
        }
    }

    public boolean isAutorizadaParaMomento(OffsetDateTime momento) {
        if (momento == null || this.exitTime == null) {
            return false;
        }
        return !momento.isBefore(this.exitTime.minusMinutes(30))
                && momento.toLocalDate().isEqual(this.exitTime.toLocalDate());
    }

    @Override
    public String resumirEvento() {
        String nomeAluno = (this.student != null) ? this.student.getFullName() : "Aluno não identificado";
        return String.format("[Saída Antecipada #%d] Aluno: %s | Motivo: %s | Horário: %s",
                this.id, nomeAluno, this.reason, this.exitTime);
    }

    // --- Getters (getId e getCreatedAt vêm de RegistroAuditavel) ---

    public Student getStudent() {
        return student;
    }

    public AuthorizedPickup getAuthorizedPickup() {
        return authorizedPickup;
    }

    public User getAuthorizedByUser() {
        return authorizedByUser;
    }

    public String getReason() {
        return reason;
    }

    public OffsetDateTime getExitTime() {
        return exitTime;
    }
}
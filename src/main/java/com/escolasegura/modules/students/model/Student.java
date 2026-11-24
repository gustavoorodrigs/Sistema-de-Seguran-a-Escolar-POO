package com.escolasegura.modules.students.model;

import com.escolasegura.shared.domain.Pessoa;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
public class Student extends Pessoa {

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Column(name = "badge_code", nullable = false, unique = true, length = 50)
    private String badgeCode;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentGuardian> guardians = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Student() {
    }

    public Student(String fullName, LocalDate birthDate, String className, String contactPhone, String badgeCode, boolean active) {
        setFullName(fullName);
        this.birthDate = birthDate;
        this.className = className;
        this.contactPhone = contactPhone;
        this.badgeCode = badgeCode;
        setActive(active);
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // --- Regras de Negócio, Encapsulamento e Polimorfismo (POO) ---

    public void vincularCracha(String novoBadgeCode) {
        if (!this.active) {
            throw new IllegalStateException("Não é possível vincular crachá a um aluno inativo.");
        }
        if (novoBadgeCode == null || novoBadgeCode.isBlank()) {
            throw new IllegalArgumentException("O código RFID do crachá não pode ser vazio.");
        }
        this.badgeCode = novoBadgeCode.trim().toUpperCase();
    }

    public void bloquearCracha() {
        String sufixo = (this.id != null) ? String.valueOf(this.id) : String.valueOf(System.nanoTime());
        this.badgeCode = "BLOQUEADO-" + sufixo;
    }

    public boolean possuiCrachaAtivo() {
        return this.active
                && this.badgeCode != null
                && !this.badgeCode.isBlank()
                && !this.badgeCode.startsWith("BLOQUEADO-");
    }

    @Override
    public String getIdentificacao() {
        return String.format("Aluno: %s | Turma: %s | Crachá: %s",
                this.fullName,
                this.className,
                possuiCrachaAtivo() ? this.badgeCode : "BLOQUEADO/INATIVO");
    }

    // --- Getters e Setters (getId, getFullName e isActive vêm de Pessoa) ---

    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("O nome completo do aluno é obrigatório.");
        }
        this.fullName = fullName.trim();
    }

    public void setActive(boolean active) {
        if (active) {
            ativar();
        } else {
            desativar();
        }
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getBadgeCode() {
        return badgeCode;
    }

    public void setBadgeCode(String badgeCode) {
        this.badgeCode = (badgeCode != null) ? badgeCode.trim().toUpperCase() : null;
    }

    public List<StudentGuardian> getGuardians() {
        return guardians;
    }

    public void setGuardians(List<StudentGuardian> guardians) {
        this.guardians = guardians;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
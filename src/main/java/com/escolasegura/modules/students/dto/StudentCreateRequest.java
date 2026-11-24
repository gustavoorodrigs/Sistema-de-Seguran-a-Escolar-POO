package com.escolasegura.modules.students.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record StudentCreateRequest(
        @NotBlank(message = "O nome completo do aluno é obrigatório.")
        @Size(max = 150, message = "O nome não pode exceder 150 caracteres.")
        String fullName,

        @NotNull(message = "A data de nascimento é obrigatória.")
        @Past(message = "A data de nascimento deve ser uma data passada.")
        LocalDate birthDate,

        @NotBlank(message = "A turma é obrigatória.")
        @Size(max = 50, message = "A identificação da turma não pode exceder 50 caracteres.")
        String className,

        @NotBlank(message = "O telefone de contato do responsável é obrigatório.")
        @Size(max = 20, message = "O telefone não pode exceder 20 caracteres.")
        String contactPhone,

        @NotBlank(message = "O código do crachá/RFID é obrigatório.")
        @Size(max = 50, message = "O código do crachá não pode exceder 50 caracteres.")
        String badgeCode,

        Boolean active
) {
    public boolean isActiveOrTrue() {
        return active == null || active;
    }
}

package com.escolasegura.modules.authorizations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AuthorizedPickupCreateRequest(
        @NotNull(message = "O ID do aluno é obrigatório.")
        Long studentId,

        @NotBlank(message = "O nome completo da pessoa autorizada é obrigatório.")
        @Size(max = 150, message = "O nome não pode exceder 150 caracteres.")
        String fullName,

        @NotBlank(message = "O parentesco/vínculo é obrigatório (ex: Avô, Tio, Vizinho, Motorista da Van).")
        @Size(max = 50, message = "O vínculo não pode exceder 50 caracteres.")
        String relationship,

        @NotBlank(message = "O telefone de contato é obrigatório.")
        @Size(max = 20, message = "O telefone não pode exceder 20 caracteres.")
        String contactPhone,

        @Size(max = 30, message = "O número do documento não pode exceder 30 caracteres.")
        String documentNumber,

        LocalDate validUntil
) {}

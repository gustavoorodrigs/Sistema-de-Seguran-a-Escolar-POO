package com.escolasegura.modules.authorizations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EarlyExitCreateRequest(
        @NotNull(message = "O ID do aluno é obrigatório.")
        Long studentId,

        @NotNull(message = "A identificação da pessoa autorizada pela retirada é obrigatória.")
        Long authorizedPickupId,

        @NotBlank(message = "A justificativa/motivo para a saída antecipada é obrigatória.")
        @Size(max = 255, message = "O motivo não pode exceder 255 caracteres.")
        String reason
) {}

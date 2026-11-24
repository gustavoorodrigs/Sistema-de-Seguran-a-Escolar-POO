package com.escolasegura.modules.authorizations.dto;

import com.escolasegura.modules.authorizations.model.PickupStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AuthorizedPickupStatusUpdateRequest(
        @NotNull(message = "A nova situação da autorização é obrigatória (VALIDA, VENCIDA ou REVOGADA).")
        PickupStatus status,

        @NotBlank(message = "A justificativa da alteração de segurança é obrigatória.")
        @Size(max = 255, message = "A justificativa não pode exceder 255 caracteres.")
        String reason
) {}

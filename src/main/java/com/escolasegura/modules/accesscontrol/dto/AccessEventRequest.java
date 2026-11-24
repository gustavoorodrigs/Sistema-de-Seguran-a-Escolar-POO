package com.escolasegura.modules.accesscontrol.dto;

import com.escolasegura.modules.accesscontrol.model.AccessDirection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record AccessEventRequest(
        @NotBlank(message = "Identificador do crachá/aluno é obrigatório")
        @Size(max = 100)
        String identifier,

        @NotNull(message = "Direção do acesso (ENTRADA/SAIDA) é obrigatória")
        AccessDirection direction,

        OffsetDateTime timestamp
) {}
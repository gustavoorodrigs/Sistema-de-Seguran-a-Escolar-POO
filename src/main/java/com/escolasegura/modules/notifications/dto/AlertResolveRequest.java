package com.escolasegura.modules.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlertResolveRequest(
        @NotBlank(message = "Informe a tratativa ou justificativa de encerramento do alerta")
        @Size(max = 500)
        String resolutionNotes
) {}
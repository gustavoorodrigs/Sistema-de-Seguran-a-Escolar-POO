package com.escolasegura.modules.students.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GuardianLinkRequest(
        @NotNull(message = "O ID do usuário responsável é obrigatório.")
        Long guardianUserId,

        @NotBlank(message = "O vínculo de parentesco é obrigatório (ex: Mãe, Pai, Responsável Legal).")
        @Size(max = 50, message = "O vínculo não pode exceder 50 caracteres.")
        String relationship
) {}

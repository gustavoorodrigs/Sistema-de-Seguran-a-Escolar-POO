package com.escolasegura.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuardianCreateRequest(
        @NotBlank(message = "O nome do responsável é obrigatório.")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
        String name,

        @NotBlank(message = "O e-mail do responsável é obrigatório.")
        @Email(message = "Informe um endereço de e-mail válido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String email,

        @NotBlank(message = "A senha inicial é obrigatória.")
        @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres.")
        String password,

        Long studentId,

        @Size(max = 50, message = "O parentesco deve ter no máximo 50 caracteres.")
        String relationship
) {
}
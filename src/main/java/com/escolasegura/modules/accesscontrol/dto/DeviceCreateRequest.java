package com.escolasegura.modules.accesscontrol.dto;

import com.escolasegura.modules.accesscontrol.model.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeviceCreateRequest(
        @NotBlank(message = "Nome do dispositivo é obrigatório")
        @Size(max = 100)
        String name,

        @NotNull(message = "Tipo do dispositivo é obrigatório")
        DeviceType deviceType,

        @NotBlank(message = "Localização do dispositivo é obrigatória")
        @Size(max = 150)
        String location
) {}
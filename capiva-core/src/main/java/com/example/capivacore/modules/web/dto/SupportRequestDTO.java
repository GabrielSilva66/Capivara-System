package com.example.capivacore.modules.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SupportRequestDTO(

        String conversationId,

        @NotBlank(message = "userName é obrigatório")
        String userName,

        @NotBlank(message = "title é obrigatório")
        String title,

        @NotBlank(message = "description é obrigatória")
        String description,

        @NotBlank(message = "severity é obrigatória")
        @Pattern(regexp = "BAIXA|MEDIA|ALTA|CRITICA",
                 message = "severity deve ser: BAIXA, MEDIA, ALTA ou CRITICA")
        String severity,

        @NotBlank(message = "message é obrigatória")
        String message
) {}

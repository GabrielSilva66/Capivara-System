package com.example.capivacore.modules.web.dto;

import jakarta.validation.constraints.NotBlank;

public record TicketRequestDTO(

     @NotBlank()
     String userName,

     @NotBlank()
     String title,

     @NotBlank()
     String description
) {
}

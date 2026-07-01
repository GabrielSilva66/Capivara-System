package br.ufrn.imd.capivaai.module.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AskInputDTO {

    @NotBlank
    private String ticketId;

    @NotBlank
    private String userName;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String severity;
}

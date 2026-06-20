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
    private String conversationId;

    @NotBlank
    private String message;
}

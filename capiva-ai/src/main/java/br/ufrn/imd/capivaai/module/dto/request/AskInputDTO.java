package br.ufrn.imd.capivaai.module.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Input GraphQL para a mutation {@code ask}.
 */
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

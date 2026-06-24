package br.ufrn.imd.capivaai.module.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO de entrada para a mutation {@code ask} do GraphQL.
 * <p>
 * Contém a mensagem livre do usuário mais os dados estruturados do chamado
 * preenchidos via formulário no MS1. Esses dados são injetados como contexto
 * no prompt da IA, permitindo que ela crie o ticket e a issue no GitHub
 * sem precisar perguntar ao usuário durante o chat.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AskInputDTO {

    @NotBlank
    private String conversationId;

    /** Nome do usuário que abriu o chamado. */
    @NotBlank
    private String userName;

    /** Título curto descritivo do problema. */
    @NotBlank
    private String title;

    /** Descrição completa do problema relatado. */
    @NotBlank
    private String description;

    /** Severidade: BAIXA, MEDIA, ALTA ou CRITICA. */
    @NotBlank
    private String severity;
}

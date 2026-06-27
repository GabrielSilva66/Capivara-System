package br.ufrn.imd.capivaai.module.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO de entrada para a mutation {@code ask} do GraphQL.
 * <p>
 * Contém o {@code ticketId} gerado pelo capiva-core (banco) e os dados
 * estruturados do chamado. O ticketId é usado como:
 * <ul>
 *   <li>ID de conversação na ChatMemory (contexto de diálogo)</li>
 *   <li>Referência ao criar a issue no GitHub</li>
 *   <li>Chave que o serverless de escalonamento usa para atualizar severidade</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AskInputDTO {

    /** ID do ticket gerado pelo PostgreSQL no capiva-core (ex: TICKET_000001). */
    @NotBlank
    private String ticketId;

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

package br.ufrn.imd.capivaai.module.dto.response;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponseDTO {
    private String conversationId;
    private String answer;
}

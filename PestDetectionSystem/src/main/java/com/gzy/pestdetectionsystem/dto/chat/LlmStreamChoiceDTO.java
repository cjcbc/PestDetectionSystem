package com.gzy.pestdetectionsystem.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmStreamChoiceDTO {
    private Integer index;
    private LlmChoiceMessageDTO delta;

    @JsonProperty("finish_reason")
    private String finishReason;
}

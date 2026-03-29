package com.gzy.pestdetectionsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmChoiceDTO {
    private Integer index;
    private LlmChoiceMessageDTO message;

    @JsonProperty("finish_reason")
    private String finishReason;
}

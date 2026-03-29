package com.gzy.pestdetectionsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LlmUsageDTO {
    @JsonProperty("total_tokens")
    private Integer totalTokens;

    @JsonProperty("total_characters")
    private Integer totalCharacters;

    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    @JsonProperty("completion_tokens_details")
    private LlmCompletionDetailsDTO completionDetailsDTO;
}

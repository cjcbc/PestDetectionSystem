package com.gzy.pestdetectionsystem.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmCompletionDetailsDTO {
    @JsonProperty("reasoning_tokens")
    private Integer reasoningTokens;
}

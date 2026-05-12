package com.gzy.pestdetectionsystem.dto.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LlmChoiceMessageDTO {
    private String role;
    private String content;
    private String name;

    @JsonProperty("reasoning_content")
    private String reasoningContent;

    @JsonProperty("audio_content")
    private String audioContent;
}

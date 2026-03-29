package com.gzy.pestdetectionsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LlmChoiceMessageDTO {
    private String role;
    private String content;
    private String name;

    @JsonProperty("audio_content")
    private String audioContent;
}

package com.gzy.pestdetectionsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmChatResponseDTO {
    private String id;
    private List<LlmChoiceDTO> choices;
    private Long created;
    private String model;
    private String object;
    private LlmUsageDTO usage;

    @JsonProperty("base_resp")
    private LlmBaseRespDTO baseResp;
}

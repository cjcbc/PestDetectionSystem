package com.gzy.pestdetectionsystem.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SSE 流式响应中每个 chunk 的数据结构（OpenAI 兼容格式）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LlmStreamChunkDTO {
    private String id;
    private String object;
    private List<LlmStreamChoiceDTO> choices;
    private Long created;
    private String model;
    private LlmUsageDTO usage;
}

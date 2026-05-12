package com.gzy.pestdetectionsystem.dto.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LlmChoiceMessageDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeReasoningContentFromSnakeCaseField() throws Exception {
        String json = "{\"role\":\"assistant\",\"reasoning_content\":\"正在分析叶片症状\"}";

        LlmChoiceMessageDTO message = objectMapper.readValue(json, LlmChoiceMessageDTO.class);

        assertEquals("assistant", message.getRole());
        assertEquals("正在分析叶片症状", message.getReasoningContent());
    }
}

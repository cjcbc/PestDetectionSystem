package com.gzy.pestdetectionsystem.service;

import com.gzy.pestdetectionsystem.dto.LlmChatResponseDTO;
import com.gzy.pestdetectionsystem.dto.LlmMessageDTO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface LlmService {

    /**
     * 基础对话（单轮）
     */
    LlmChatResponseDTO chat(List<LlmMessageDTO> messages);

    /**
     * 连续对话（多轮），携带历史上下文
     *
     * @param systemPrompt  系统提示词
     * @param historyMessages 历史消息列表（role=user/assistant 交替）
     * @param userMessage   本轮用户消息
     * @return LLM 响应
     */
    LlmChatResponseDTO chatWithContext(String systemPrompt,
                                       List<LlmMessageDTO> historyMessages,
                                       String userMessage);

    /**
     * 流式连续对话，返回 SSE 原始行数据流
     */
    Flux<String> chatWithContextStream(String systemPrompt,
                                       List<LlmMessageDTO> historyMessages,
                                       String userMessage);
}

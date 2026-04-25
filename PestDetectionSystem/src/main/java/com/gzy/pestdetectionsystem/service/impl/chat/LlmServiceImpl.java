package com.gzy.pestdetectionsystem.service.impl.chat;

import com.gzy.pestdetectionsystem.config.llm.LlmProperties;
import com.gzy.pestdetectionsystem.dto.chat.LlmChatRequestDTO;
import com.gzy.pestdetectionsystem.dto.chat.LlmChatResponseDTO;
import com.gzy.pestdetectionsystem.dto.chat.LlmMessageDTO;
import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.exception.CommonErrorCode;
import com.gzy.pestdetectionsystem.service.chat.LlmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LlmServiceImpl implements LlmService {

    private final LlmProperties llmProperties;
    private final WebClient llmWebClient;

    public LlmServiceImpl(LlmProperties llmProperties,
                          @Qualifier("llmWebClient") WebClient llmWebClient) {
        this.llmProperties = llmProperties;
        this.llmWebClient = llmWebClient;
    }

    @Override
    public LlmChatResponseDTO chat(List<LlmMessageDTO> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new BusinessException(CommonErrorCode.LLM_PARAM_INVALID);
        }
        if (messages.size() == 1 && "system".equals(messages.get(0).getRole())) {
            throw new BusinessException(CommonErrorCode.LLM_PARAM_INVALID);
        }
        return doCall(messages);
    }

    @Override
    public LlmChatResponseDTO chatWithContext(String systemPrompt,
                                               List<LlmMessageDTO> historyMessages,
                                               String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            throw new BusinessException(CommonErrorCode.LLM_PARAM_INVALID);
        }

        List<LlmMessageDTO> messages = new ArrayList<>();

        // 1. 系统提示词
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new LlmMessageDTO("system", systemPrompt));
        }

        // 2. 历史消息（上下文）
        if (historyMessages != null && !historyMessages.isEmpty()) {
            messages.addAll(historyMessages);
        }

        // 3. 本轮用户消息
        messages.add(new LlmMessageDTO("user", userMessage));

        return doCall(messages);
    }

    @Override
    public Flux<String> chatWithContextStream(String systemPrompt,
                                               List<LlmMessageDTO> historyMessages,
                                               String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            throw new BusinessException(CommonErrorCode.LLM_PARAM_INVALID);
        }

        List<LlmMessageDTO> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new LlmMessageDTO("system", systemPrompt));
        }
        if (historyMessages != null && !historyMessages.isEmpty()) {
            messages.addAll(historyMessages);
        }
        messages.add(new LlmMessageDTO("user", userMessage));

        return doCallStream(messages);
    }

    /**
     * 统一执行 LLM HTTP 调用
     */
    private LlmChatResponseDTO doCall(List<LlmMessageDTO> messages) {
        if (llmProperties.getApiKey() == null || llmProperties.getApiKey().isBlank()) {
            throw new BusinessException(CommonErrorCode.LLM_API_KEY_MISSING);
        }

        LlmChatRequestDTO requestDTO = new LlmChatRequestDTO();
        requestDTO.setModel(llmProperties.getModel());
        requestDTO.setMessages(messages);
        requestDTO.setTemperature(llmProperties.getTemperature());
        requestDTO.setMaxTokens(llmProperties.getMaxTokens());

        long startTime = System.currentTimeMillis();
        try {
            log.info("[LLM] 发起请求 model={}, messageCount={}", llmProperties.getModel(), messages.size());

            LlmChatResponseDTO response = llmWebClient.post()
                    .uri(llmProperties.getChatPath())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + llmProperties.getApiKey())
                    .bodyValue(requestDTO)
                    .retrieve()
                    .bodyToMono(LlmChatResponseDTO.class)
                    .block();

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                throw new BusinessException(CommonErrorCode.LLM_RESPONSE_EMPTY);
            }

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[LLM] 请求成功 耗时={}ms, finishReason={}, totalTokens={}",
                    elapsed,
                    response.getChoices().get(0).getFinishReason(),
                    response.getUsage() != null ? response.getUsage().getTotalTokens() : "N/A");

            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (WebClientResponseException e) {
            log.error("[LLM] HTTP错误 status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(CommonErrorCode.LLM_CALL_FAILED,
                    "LLM服务返回错误: HTTP " + e.getStatusCode().value());
        } catch (Exception e) {
            log.error("[LLM] 调用失败: {}", e.getMessage(), e);
            throw new BusinessException(CommonErrorCode.LLM_CALL_FAILED, "调用LLM失败: " + e.getMessage());
        }
    }

    /**
     * 流式调用 LLM，返回 SSE 原始行数据
     */
    private Flux<String> doCallStream(List<LlmMessageDTO> messages) {
        if (llmProperties.getApiKey() == null || llmProperties.getApiKey().isBlank()) {
            throw new BusinessException(CommonErrorCode.LLM_API_KEY_MISSING);
        }

        LlmChatRequestDTO requestDTO = new LlmChatRequestDTO();
        requestDTO.setModel(llmProperties.getModel());
        requestDTO.setMessages(messages);
        requestDTO.setTemperature(llmProperties.getTemperature());
        requestDTO.setMaxTokens(llmProperties.getMaxTokens());
        requestDTO.setStream(true);

        log.info("[LLM] 发起流式请求 model={}, messageCount={}", llmProperties.getModel(), messages.size());

        return llmWebClient.post()
                .uri(llmProperties.getChatPath())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + llmProperties.getApiKey())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToFlux(String.class);
    }
}

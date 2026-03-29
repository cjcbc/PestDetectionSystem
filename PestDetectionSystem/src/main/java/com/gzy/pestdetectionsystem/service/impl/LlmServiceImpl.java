package com.gzy.pestdetectionsystem.service.impl;

import com.gzy.pestdetectionsystem.config.LlmProperties;
import com.gzy.pestdetectionsystem.dto.LlmChatRequestDTO;
import com.gzy.pestdetectionsystem.dto.LlmChatResponseDTO;
import com.gzy.pestdetectionsystem.dto.LlmMessageDTO;
import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.exception.CommonErrorCode;
import com.gzy.pestdetectionsystem.service.LlmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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

        if (messages.size() == 1 && messages.get(0).getRole().equals("system")){
            throw new BusinessException(CommonErrorCode.LLM_RESPONSE_EMPTY);
        }

        if (llmProperties.getApiKey() == null || llmProperties.getApiKey().isBlank()) {
            throw new BusinessException(CommonErrorCode.LLM_API_KEY_MISSING);
        }

        LlmChatRequestDTO requestDTO = new LlmChatRequestDTO();
        requestDTO.setModel(llmProperties.getModel());
        requestDTO.setMessages(messages);
        requestDTO.setTemperature(llmProperties.getTemperature());
        requestDTO.setMaxTokens(llmProperties.getMaxTokens());

        try {
            log.info("调用LLM接口, model={}, messageCount={}", llmProperties.getModel(), messages.size());

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

            return response;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用LLM失败: {}", e.getMessage(), e);
            throw new BusinessException(CommonErrorCode.LLM_CALL_FAILED, "调用LLM失败: " + e.getMessage());
        }
    }
}

package com.gzy.pestdetectionsystem.service;

import com.gzy.pestdetectionsystem.dto.LlmChatResponseDTO;
import com.gzy.pestdetectionsystem.dto.LlmMessageDTO;

import java.util.List;

public interface LlmService {
    LlmChatResponseDTO chat(List<LlmMessageDTO> messages);
}

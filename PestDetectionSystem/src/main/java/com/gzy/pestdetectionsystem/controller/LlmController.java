package com.gzy.pestdetectionsystem.controller;

import com.gzy.pestdetectionsystem.dto.LlmChatResponseDTO;
import com.gzy.pestdetectionsystem.dto.LlmMessageDTO;
import com.gzy.pestdetectionsystem.service.LlmService;
import com.gzy.pestdetectionsystem.utils.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/llm")
public class LlmController {
    private static final String SYSTEM_PROMPT = "你是农业病虫害专家助手，请使用中文简洁回答。";

    private final LlmService llmService;

    public LlmController(LlmService llmService) {
        this.llmService = llmService;
    }

    @PostMapping("/test")
    public Result<LlmChatResponseDTO> test(@RequestParam String message) {
        List<LlmMessageDTO> messages = List.of(
                new LlmMessageDTO("system", SYSTEM_PROMPT),
                new LlmMessageDTO("user", message)
        );
        return Result.ok(llmService.chat(messages));
    }
}

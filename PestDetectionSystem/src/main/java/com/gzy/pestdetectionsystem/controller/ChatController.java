package com.gzy.pestdetectionsystem.controller;

import com.gzy.pestdetectionsystem.dto.CreateChatSessionDTO;
import com.gzy.pestdetectionsystem.dto.SendChatMessageDTO;
import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.exception.CommonErrorCode;
import com.gzy.pestdetectionsystem.service.ChatService;
import com.gzy.pestdetectionsystem.utils.Result;
import com.gzy.pestdetectionsystem.vo.ChatMessageVO;
import com.gzy.pestdetectionsystem.vo.ChatReplyVO;
import com.gzy.pestdetectionsystem.vo.ChatSessionVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 创建对话会话
     * POST /api/chat/session
     */
    @PostMapping("/session")
    public Result<ChatSessionVO> createSession(@RequestBody CreateChatSessionDTO dto,
                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(chatService.createSession(userId, dto));
    }

    /**
     * 获取当前用户的所有会话列表（按最近消息时间倒序）
     * GET /api/chat/sessions
     */
    @GetMapping("/sessions")
    public Result<List<ChatSessionVO>> getSessions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(chatService.getSessions(userId));
    }

    /**
     * 获取指定会话的消息历史
     * GET /api/chat/session/{sessionId}/messages
     */
    @GetMapping("/session/{sessionId}/messages")
    public Result<List<ChatMessageVO>> getMessages(@PathVariable String sessionId,
                                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            return Result.ok(chatService.getMessages(userId, Long.valueOf(sessionId)));
        } catch (NumberFormatException e) {
            throw new BusinessException(CommonErrorCode.LLM_PARAM_INVALID, "Invalid sessionId format");
        }
    }

    /**
     * 发送消息（核心接口，支持连续对话）
     * POST /api/chat/send
     */
    @PostMapping("/send")
    public Result<ChatReplyVO> sendMessage(@RequestBody SendChatMessageDTO dto,
                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(chatService.sendMessage(userId, dto));
    }

    /**
     * 流式发送消息（SSE）
     * POST /api/chat/send/stream
     */
    @PostMapping(value = "/send/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStream(@RequestBody SendChatMessageDTO dto,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return chatService.sendMessageStream(userId, dto);
    }

    /**
     * 删除对话会话（逻辑删除）
     * DELETE /api/chat/session/{sessionId}
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<?> deleteSession(@PathVariable String sessionId,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            chatService.deleteSession(userId, Long.valueOf(sessionId));
        } catch (NumberFormatException e) {
            throw new BusinessException(CommonErrorCode.LLM_PARAM_INVALID, "Invalid sessionId format");
        }
        return Result.ok("删除成功");
    }

    /**
     *  获取对话额度
     *  GET /api/chat/quota
     */
    @GetMapping("/quota")
    public Result<?> getQuota(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(chatService.getQuota(userId));
    }
}

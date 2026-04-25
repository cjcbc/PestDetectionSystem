package com.gzy.pestdetectionsystem.service.chat;

import com.gzy.pestdetectionsystem.dto.chat.CreateChatSessionDTO;
import com.gzy.pestdetectionsystem.dto.chat.SendChatMessageDTO;
import com.gzy.pestdetectionsystem.vo.chat.ChatMessageVO;
import com.gzy.pestdetectionsystem.vo.chat.ChatQuotaVO;
import com.gzy.pestdetectionsystem.vo.chat.ChatReplyVO;
import com.gzy.pestdetectionsystem.vo.chat.ChatSessionVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface ChatService {

    /**
     * 创建对话
    */
    ChatSessionVO createSession(Long userId, CreateChatSessionDTO dto);

    /**
     * 获取对话
     */
    List<ChatSessionVO> getSessions(Long userId);

    /**
     * 获取对话消息
     */
    List<ChatMessageVO> getMessages(Long userId, Long sessionId);

    /**
     * 核心 发送对话
     */
    ChatReplyVO sendMessage(Long userId, SendChatMessageDTO dto);

    /**
     * 流式发送对话
     */
    SseEmitter sendMessageStream(Long userId, SendChatMessageDTO dto);

    /**
     *  删除对话
     */
    void deleteSession(Long userId, Long sessionId);

    /**
     * 获取用户配额
     */
    ChatQuotaVO getQuota(Long userId);
}

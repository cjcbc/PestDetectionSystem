package com.gzy.pestdetectionsystem.service;

import com.gzy.pestdetectionsystem.dto.CreateChatSessionDTO;
import com.gzy.pestdetectionsystem.dto.SendChatMessageDTO;
import com.gzy.pestdetectionsystem.vo.ChatMessageVO;
import com.gzy.pestdetectionsystem.vo.ChatReplyVO;
import com.gzy.pestdetectionsystem.vo.ChatSessionVO;

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
     *  删除对话
     */
    void deleteSession(Long userId, Long sessionId);
}

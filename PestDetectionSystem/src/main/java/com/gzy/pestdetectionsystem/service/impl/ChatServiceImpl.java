package com.gzy.pestdetectionsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gzy.pestdetectionsystem.dto.CreateChatSessionDTO;
import com.gzy.pestdetectionsystem.dto.LlmChatResponseDTO;
import com.gzy.pestdetectionsystem.dto.LlmMessageDTO;
import com.gzy.pestdetectionsystem.dto.SendChatMessageDTO;
import com.gzy.pestdetectionsystem.entity.ChatMessage;
import com.gzy.pestdetectionsystem.entity.ChatQuotaDaily;
import com.gzy.pestdetectionsystem.entity.ChatSession;
import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.exception.CommonErrorCode;
import com.gzy.pestdetectionsystem.mapper.ChatMessageMapper;
import com.gzy.pestdetectionsystem.mapper.ChatQuotaDailyMapper;
import com.gzy.pestdetectionsystem.mapper.ChatSessionMapper;
import com.gzy.pestdetectionsystem.service.ChatService;
import com.gzy.pestdetectionsystem.service.LlmService;
import com.gzy.pestdetectionsystem.utils.SnowflakeIdGenerator;
import com.gzy.pestdetectionsystem.vo.ChatMessageVO;
import com.gzy.pestdetectionsystem.vo.ChatQuotaVO;
import com.gzy.pestdetectionsystem.vo.ChatReplyVO;
import com.gzy.pestdetectionsystem.vo.ChatSessionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final String SYSTEM_PROMPT = "你是农业病虫害专家助手，请使用中文简洁、专业地回答用户关于病虫害识别、防治、农药使用等问题。";

    /** 每次调用 LLM 时携带的最大历史消息条数（user+assistant 各算一条） */
    private static final int MAX_HISTORY_MESSAGES = 20;

    /** 每日最大请求次数（0 表示不限制） */
    @Value("${chat.quota.daily-max-requests:50}")
    private int dailyMaxRequests;

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatQuotaDailyMapper chatQuotaDailyMapper;
    private final LlmService llmService;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    // ─────────────────────────────────────────────────────────────────────────
    // 会话管理
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatSessionVO createSession(Long userId, CreateChatSessionDTO dto) {
        long now = System.currentTimeMillis();

        ChatSession session = new ChatSession();
        session.setId(snowflakeIdGenerator.nextId());
        session.setUserId(userId);
        session.setTitle(dto.getTitle() != null && !dto.getTitle().isBlank()
                ? dto.getTitle() : "新对话");
        session.setScene(dto.getScene() != null && !dto.getScene().isBlank()
                ? dto.getScene() : "expert");
        session.setMessageCount(0);
        session.setLastMessageAt(now);
        session.setCreatedTime(now);
        session.setUpdatedTime(now);
        session.setDeleted(0);

        chatSessionMapper.insert(session);
        log.info("[Chat] 创建会话 sessionId={}, userId={}", session.getId(), userId);
        return toSessionVO(session);
    }

    @Override
    public List<ChatSessionVO> getSessions(Long userId) {
        List<ChatSession> sessions = chatSessionMapper.selectList(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .eq(ChatSession::getDeleted, 0)
                        .orderByDesc(ChatSession::getLastMessageAt)
        );
        return sessions.stream().map(this::toSessionVO).collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageVO> getMessages(Long userId, Long sessionId) {
        // 校验会话归属
        validateSessionOwner(userId, sessionId);

        List<ChatMessage> messages = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .eq(ChatMessage::getUserId, userId)
                        .orderByAsc(ChatMessage::getCreatedTime)
        );
        return messages.stream().map(this::toMessageVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long userId, Long sessionId) {
        ChatSession session = validateSessionOwner(userId, sessionId);
        session.setDeleted(1);
        session.setUpdatedTime(System.currentTimeMillis());
        chatSessionMapper.updateById(session);
        log.info("[Chat] 删除会话 sessionId={}, userId={}", sessionId, userId);
    }

    @Override
    public ChatQuotaVO getQuota(Long userId) {
        String today = LocalDate.now().toString();
        ChatQuotaDaily quota = chatQuotaDailyMapper.selectOne(
                new LambdaQueryWrapper<ChatQuotaDaily>()
                        .eq(ChatQuotaDaily::getUserId, userId)
                        .eq(ChatQuotaDaily::getQuotaDate, today)
        );

        if (quota == null) {
            return new ChatQuotaVO(today, 0, 0, 0);
        }

        return new ChatQuotaVO(
                quota.getQuotaDate(),
                safeInt(quota.getRequestCount()),
                safeInt(quota.getInputTokens()),
                safeInt(quota.getOutputTokens())
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 核心：发送消息
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatReplyVO sendMessage(Long userId, SendChatMessageDTO dto) {
        if (dto.getMessage() == null || dto.getMessage().isBlank()) {
            throw new BusinessException(CommonErrorCode.LLM_PARAM_INVALID);
        }

        // 1. 校验会话归属
        ChatSession session = validateSessionOwner(userId, dto.getSessionId());

        // 2. 检查每日配额
        checkAndGetQuota(userId);

        long now = System.currentTimeMillis();

        // 3. 保存用户消息
        ChatMessage userMsg = buildMessage(session.getId(), userId, "user",
                dto.getMessage(), null, 0, 0, 0, 1, now);
        chatMessageMapper.insert(userMsg);

        // 4. 构建历史上下文（取最近 MAX_HISTORY_MESSAGES 条已成功的消息）
        List<LlmMessageDTO> history = buildHistory(userId, session.getId());

        // 5. 调用 LLM
        LlmChatResponseDTO llmResp;
        try {
            llmResp = llmService.chatWithContext(SYSTEM_PROMPT, history, dto.getMessage());
        } catch (BusinessException e) {
            // 标记用户消息为失败
            userMsg.setStatus(0);
            chatMessageMapper.updateById(userMsg);
            throw e;
        }

        // 6. 解析 LLM 响应
        String answer = llmResp.getChoices().get(0).getMessage().getContent();
        int promptTokens = 0, completionTokens = 0, totalTokens = 0;
        if (llmResp.getUsage() != null) {
            promptTokens = safeInt(llmResp.getUsage().getPromptTokens());
            completionTokens = safeInt(llmResp.getUsage().getCompletionTokens());
            totalTokens = safeInt(llmResp.getUsage().getTotalTokens());
        }

        // 7. 保存 AI 回复
        long replyTime = System.currentTimeMillis();
        ChatMessage assistantMsg = buildMessage(session.getId(), userId, "assistant",
                answer, llmResp.getModel(), promptTokens, completionTokens, totalTokens, 1, replyTime);
        chatMessageMapper.insert(assistantMsg);

        // 8. 更新会话统计
        session.setMessageCount(session.getMessageCount() + 2);
        session.setLastMessageAt(replyTime);
        session.setUpdatedTime(replyTime);
        // 自动生成标题（首次对话时取用户消息前20字）
        if (session.getMessageCount() == 2) {
            String autoTitle = dto.getMessage().length() > 20
                    ? dto.getMessage().substring(0, 20) + "…"
                    : dto.getMessage();
            session.setTitle(autoTitle);
        }
        chatSessionMapper.updateById(session);

        // 9. 更新每日配额
        updateQuota(userId, promptTokens, completionTokens);

        log.info("[Chat] 消息发送成功 sessionId={}, userId={}, totalTokens={}", session.getId(), userId, totalTokens);

        return new ChatReplyVO(session.getId(), answer, promptTokens, completionTokens, totalTokens);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 私有辅助方法
    // ─────────────────────────────────────────────────────────────────────────

    /** 校验会话归属，返回会话实体 */
    private ChatSession validateSessionOwner(Long userId, Long sessionId) {
        if (sessionId == null) {
            throw new BusinessException(CommonErrorCode.LLM_PARAM_INVALID);
        }
        ChatSession session = chatSessionMapper.selectOne(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getId, sessionId)
                        .eq(ChatSession::getDeleted, 0)
        );
        if (session == null) {
            throw new BusinessException(CommonErrorCode.CHAT_SESSION_NOT_FOUND);
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }
        return session;
    }

    /** 检查每日配额 */
    private void checkAndGetQuota(Long userId) {
        if (dailyMaxRequests <= 0) return;

        String today = LocalDate.now().toString();
        ChatQuotaDaily quota = chatQuotaDailyMapper.selectOne(
                new LambdaQueryWrapper<ChatQuotaDaily>()
                        .eq(ChatQuotaDaily::getUserId, userId)
                        .eq(ChatQuotaDaily::getQuotaDate, today)
        );
        if (quota != null && quota.getRequestCount() >= dailyMaxRequests) {
            throw new BusinessException(CommonErrorCode.CHAT_QUOTA_EXCEEDED);
        }
    }

    /** 更新每日配额统计 */
    private void updateQuota(Long userId, int promptTokens, int completionTokens) {
        String today = LocalDate.now().toString();
        long now = System.currentTimeMillis();

        ChatQuotaDaily quota = chatQuotaDailyMapper.selectOne(
                new LambdaQueryWrapper<ChatQuotaDaily>()
                        .eq(ChatQuotaDaily::getUserId, userId)
                        .eq(ChatQuotaDaily::getQuotaDate, today)
        );

        if (quota == null) {
            quota = new ChatQuotaDaily();
            quota.setId(snowflakeIdGenerator.nextId());
            quota.setUserId(userId);
            quota.setQuotaDate(today);
            quota.setRequestCount(1);
            quota.setInputTokens(promptTokens);
            quota.setOutputTokens(completionTokens);
            quota.setCreatedTime(now);
            quota.setUpdatedTime(now);
            chatQuotaDailyMapper.insert(quota);
        } else {
            quota.setRequestCount(quota.getRequestCount() + 1);
            quota.setInputTokens(quota.getInputTokens() + promptTokens);
            quota.setOutputTokens(quota.getOutputTokens() + completionTokens);
            quota.setUpdatedTime(now);
            chatQuotaDailyMapper.updateById(quota);
        }
    }

    /** 构建历史上下文消息（仅取 user/assistant 角色，排除 system） */
    private List<LlmMessageDTO> buildHistory(Long userId, Long sessionId) {
        List<ChatMessage> recent = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .eq(ChatMessage::getUserId, userId)
                        .eq(ChatMessage::getStatus, 1)
                        .in(ChatMessage::getRole, "user", "assistant")
                        .orderByDesc(ChatMessage::getCreatedTime)
                        .last("LIMIT " + MAX_HISTORY_MESSAGES)
        );

        // 反转为正序（时间从早到晚）
        List<ChatMessage> ordered = recent.stream()
                .sorted((a, b) -> Long.compare(a.getCreatedTime(), b.getCreatedTime()))
                .collect(Collectors.toList());

        return ordered.stream()
                .map(m -> new LlmMessageDTO(m.getRole(), m.getContent()))
                .collect(Collectors.toList());
    }

    /** 构建 ChatMessage 实体 */
    private ChatMessage buildMessage(Long sessionId, Long userId, String role,
                                     String content, String model,
                                     int promptTokens, int completionTokens, int totalTokens,
                                     int status, long createdTime) {
        ChatMessage msg = new ChatMessage();
        msg.setId(snowflakeIdGenerator.nextId());
        msg.setSessionId(sessionId);
        msg.setUserId(userId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setModel(model);
        msg.setPromptTokens(promptTokens);
        msg.setCompletionTokens(completionTokens);
        msg.setTotalTokens(totalTokens);
        msg.setStatus(status);
        msg.setCreatedTime(createdTime);
        return msg;
    }

    private int safeInt(Integer val) {
        return val != null ? val : 0;
    }

    private int safeInt(Long val) {
        return val != null ? val.intValue() : 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VO 转换
    // ─────────────────────────────────────────────────────────────────────────

    private ChatSessionVO toSessionVO(ChatSession s) {
        return new ChatSessionVO(s.getId(), s.getTitle(), s.getScene(),
                s.getMessageCount(), s.getLastMessageAt(), s.getCreatedTime(), s.getUpdatedTime());
    }

    private ChatMessageVO toMessageVO(ChatMessage m) {
        return new ChatMessageVO(m.getId(), m.getSessionId(), m.getRole(), m.getContent(),
                m.getModel(), m.getPromptTokens(), m.getCompletionTokens(),
                m.getTotalTokens(), m.getCreatedTime());
    }
}

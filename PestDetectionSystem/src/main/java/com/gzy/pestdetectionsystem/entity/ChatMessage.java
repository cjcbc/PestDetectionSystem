package com.gzy.pestdetectionsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_message")
@Schema(description = "对话消息实体")
public class ChatMessage {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long sessionId;
    private Long userId;
    private Long detectionId;
    private String role;
    private String content;
    private String model;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Integer status;
    private Long createdTime;
}

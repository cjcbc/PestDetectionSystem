package com.gzy.pestdetectionsystem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "对话消息返回对象")
public class ChatMessageVO {
    private String id;
    private String sessionId;
    private String detectionId;
    private String role;
    private String content;
    private String model;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Long createdTime;
}

package com.gzy.pestdetectionsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "发送对话消息参数")
public class SendChatMessageDTO {
    private String sessionId;
    private String message;
    private String detectionId;
}

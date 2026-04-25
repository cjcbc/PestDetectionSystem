package com.gzy.pestdetectionsystem.vo.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "发送消息后的回复对象")
public class ChatReplyVO {
    private String sessionId;
    private String answer;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
}

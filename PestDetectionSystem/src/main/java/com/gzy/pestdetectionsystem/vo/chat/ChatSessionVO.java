package com.gzy.pestdetectionsystem.vo.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "对话会话返回对象")
public class ChatSessionVO {
    private String id;
    private String title;
    private String scene;
    private Integer messageCount;
    private Long lastMessageAt;
    private Long createdTime;
    private Long updatedTime;
}

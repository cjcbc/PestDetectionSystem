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
@TableName("chat_session")
@Schema(description = "对话会话实体")
public class ChatSession {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long userId;
    private String title;
    private String scene;
    private String summary;
    private Integer messageCount;
    private Long lastMessageAt;
    private Long createdTime;
    private Long updatedTime;
    private Integer deleted;
}

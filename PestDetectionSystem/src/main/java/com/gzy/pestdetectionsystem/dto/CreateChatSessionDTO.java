package com.gzy.pestdetectionsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建对话会话参数")
public class CreateChatSessionDTO {
    private String title;
    private String scene;
}

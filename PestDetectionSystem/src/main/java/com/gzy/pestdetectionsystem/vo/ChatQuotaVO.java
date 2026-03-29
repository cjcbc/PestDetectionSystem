package com.gzy.pestdetectionsystem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "对话额度返回对象")
public class ChatQuotaVO {
    private String quotaDate;
    private Integer requestCount;
    private Integer inputTokens;
    private Integer outputTokens;
}

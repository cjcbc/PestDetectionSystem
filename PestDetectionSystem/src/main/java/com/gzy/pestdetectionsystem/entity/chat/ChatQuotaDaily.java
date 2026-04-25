package com.gzy.pestdetectionsystem.entity.chat;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_quota_daily")
@Schema(description = "对话每日额度实体")
public class ChatQuotaDaily {
    private Long id;
    private Long userId;
    private String quotaDate;
    private Integer requestCount;
    private Integer inputTokens;
    private Integer outputTokens;
    private Long createdTime;
    private Long updatedTime;
}

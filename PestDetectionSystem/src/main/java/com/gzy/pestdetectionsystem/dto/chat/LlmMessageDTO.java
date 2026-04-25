package com.gzy.pestdetectionsystem.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LlmMessageDTO {
    private String role;
    private String content;
}

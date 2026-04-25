package com.gzy.pestdetectionsystem.dto.detection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "病虫害检测请求")
public class PestDetectionDTO {

    @Schema(description = "图片base64（不含前缀）", example = "/9j/4AAQSkZJRg...")
    private String imageBase64;
}

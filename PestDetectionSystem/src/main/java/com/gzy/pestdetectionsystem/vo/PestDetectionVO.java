package com.gzy.pestdetectionsystem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "病虫害检测结果")
public class PestDetectionVO {

    private String id;

    private String userId;

    private String imageName;

    private String topLabel;

    private BigDecimal confidence;

    /**
     * -1: 未检测到
     * 0: 低置信度
     * 1: 高置信度
     */
    private Integer status;

    private Long createdTime;
}

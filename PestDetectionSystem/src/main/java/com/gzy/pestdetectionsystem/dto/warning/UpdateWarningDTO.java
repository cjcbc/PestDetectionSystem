package com.gzy.pestdetectionsystem.dto.warning;

import lombok.Data;

@Data
public class UpdateWarningDTO {

    private String title;

    private String content;

    private String region;

    private String pestName;

    private Integer severity;

    private Integer status;
}

package com.gzy.pestdetectionsystem.dto;

import lombok.Data;

@Data
public class CreateWarningDTO {

    private String title;

    private String content;

    private String region;

    private String pestName;

    private Integer severity;

    private Integer status;
}

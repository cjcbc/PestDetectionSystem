package com.gzy.pestdetectionsystem.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarningUnreadSummaryVO {

    private String id;

    private String title;

    private Integer severity;

    private Long publishTime;
}

package com.gzy.pestdetectionsystem.vo.warning;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarningReadVO {

    private String warningId;

    private String userId;

    private Long readTime;
}

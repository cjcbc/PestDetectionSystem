package com.gzy.pestdetectionsystem.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnreadWarningCountVO {

    private Long unreadCount;

    private List<WarningUnreadSummaryVO> warnings;
}

package com.gzy.pestdetectionsystem.vo.warning;

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

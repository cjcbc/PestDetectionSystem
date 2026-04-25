package com.gzy.pestdetectionsystem.service.warning;

import com.gzy.pestdetectionsystem.dto.warning.CreateWarningDTO;
import com.gzy.pestdetectionsystem.dto.warning.UpdateWarningDTO;
import com.gzy.pestdetectionsystem.vo.article.PageVO;
import com.gzy.pestdetectionsystem.vo.warning.UnreadWarningCountVO;
import com.gzy.pestdetectionsystem.vo.warning.WarningItemVO;
import com.gzy.pestdetectionsystem.vo.warning.WarningReadVO;

public interface WarningService {

    WarningItemVO createWarning(Long publisherId, Integer roleId, CreateWarningDTO dto);

    WarningItemVO updateWarning(Long operatorId, Integer roleId, Long warningId, UpdateWarningDTO dto);

    WarningItemVO toggleWarningStatus(Long operatorId, Integer roleId, Long warningId);

    void deleteWarning(Long operatorId, Integer roleId, Long warningId);

    PageVO<WarningItemVO> getWarnings(Long userId, Integer roleId, Integer page, Integer pageSize,
                                      Integer status, Integer severity, String region, String keyword);

    WarningReadVO markRead(Long userId, Long warningId);

    UnreadWarningCountVO getUnreadCount(Long userId);

    WarningItemVO getWarningDetail(Long userId, Integer roleId, Long warningId);
}

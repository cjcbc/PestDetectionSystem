package com.gzy.pestdetectionsystem.service;

import com.gzy.pestdetectionsystem.dto.PestDetectionDTO;
import com.gzy.pestdetectionsystem.vo.PestDetectionVO;

import java.util.List;

public interface PestDetectionService {

    /**
     * 上传图片进行病虫害检测
     * @param userId 用户ID
     * @param dto 包含base64图片
     * @return 检测结果（置信度<0.7时返回null）
     */
    PestDetectionVO detect(Long userId, PestDetectionDTO dto);

    /**
     * 获取用户检测记录
     */
    List<PestDetectionVO> getRecords(Long userId);
}

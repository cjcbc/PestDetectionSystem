package com.gzy.pestdetectionsystem.controller.detection;

import com.gzy.pestdetectionsystem.dto.detection.PestDetectionDTO;
import com.gzy.pestdetectionsystem.service.detection.PestDetectionService;
import com.gzy.pestdetectionsystem.utils.Result;
import com.gzy.pestdetectionsystem.vo.detection.PestDetectionVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/detect")
@RequiredArgsConstructor
public class PestDetectionController {

    private final PestDetectionService pestDetectionService;

    /**
     * 病虫害检测
     * POST /api/detect
     */
    @PostMapping
    public Result<PestDetectionVO> detect(@RequestBody PestDetectionDTO dto,
                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PestDetectionVO result = pestDetectionService.detect(userId, dto);

        return Result.ok(result);
    }

    /**
     * 获取检测记录
     * GET /api/detect/records
     */
    @GetMapping("/records")
    public Result<List<PestDetectionVO>> getRecords(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<PestDetectionVO> records = pestDetectionService.getRecords(userId);
        return Result.ok(records);
    }
}

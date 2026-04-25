package com.gzy.pestdetectionsystem.assembler.detection;

import com.gzy.pestdetectionsystem.entity.detection.PestDetection;
import com.gzy.pestdetectionsystem.vo.detection.PestDetectionVO;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PestDetectionAssembler {

    public PestDetectionVO toVO(PestDetection detection) {
        PestDetectionVO vo = new PestDetectionVO();
        vo.setId(String.valueOf(detection.getId()));
        vo.setUserId(String.valueOf(detection.getUserId()));
        vo.setImageName(extractFileName(detection.getImageUrl()));
        vo.setTopLabel(detection.getTopLabel());
        vo.setConfidence(detection.getConfidence());
        vo.setStatus(detection.getStatus());
        vo.setCreatedTime(detection.getCreatedTime());
        return vo;
    }

    public List<PestDetectionVO> toVOList(List<PestDetection> detections) {
        return detections.stream().map(this::toVO).collect(Collectors.toList());
    }

    private String extractFileName(String path) {
        if (path == null || path.isBlank()) {
            return path;
        }
        return Paths.get(path).getFileName().toString();
    }
}

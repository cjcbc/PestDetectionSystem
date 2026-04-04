package com.gzy.pestdetectionsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gzy.pestdetectionsystem.dto.PestDetectionDTO;
import com.gzy.pestdetectionsystem.entity.PestDetection;
import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.exception.CommonErrorCode;
import com.gzy.pestdetectionsystem.mapper.PestDetectionMapper;
import com.gzy.pestdetectionsystem.service.PestDetectionService;
import com.gzy.pestdetectionsystem.vo.PestDetectionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PestDetectionServiceImpl implements PestDetectionService {

    private static final double CONFIDENCE_THRESHOLD = 0.7;
    private static final String PEST_IMAGE_DIR = "D:\\SHU files\\Graduation project\\PestDetectionSystem\\pest-images";

    private final PestDetectionMapper pestDetectionMapper;
    @Qualifier("modelWebClient")
    private final WebClient modelWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public PestDetectionVO detect(Long userId, PestDetectionDTO dto) {
        if (dto.getImageBase64() == null || dto.getImageBase64().isBlank()) {
            throw new BusinessException(CommonErrorCode.LLM_PARAM_INVALID, "图片不能为空");
        }

        // 1. 保存图片到本地
        String imagePath = saveImageToLocal(userId, dto.getImageBase64());

        // 2. 调用 Flask 检测接口
        Map<String, Object> flaskResult = callFlaskPredict(dto.getImageBase64());
        
        if (flaskResult == null || !Boolean.TRUE.equals(flaskResult.get("success"))) {
            throw new BusinessException(CommonErrorCode.LLM_PARAM_INVALID, "Flask检测服务调用失败");
        }

        // 3. 解析结果
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> predictions = (List<Map<String, Object>>) flaskResult.get("predictions");
        if (predictions == null || predictions.isEmpty()) {
            throw new BusinessException(CommonErrorCode.LLM_PARAM_INVALID, "未检测到病虫害");
        }

        // 取第一个预测结果
        Map<String, Object> prediction = predictions.get(0);
        double confidence = ((Number) prediction.get("confidence")).doubleValue();
        String className = (String) prediction.get("class_name");
        String imageName = (String) flaskResult.getOrDefault("image_name", "unknown");

        // 4. 保存记录
        PestDetection detection = new PestDetection();
        detection.setId(System.currentTimeMillis());
        detection.setUserId(userId);
        detection.setImageUrl(imagePath);
        detection.setOriginalFileName(imageName);
        detection.setResultJson(toJsonString(flaskResult));
        detection.setTopLabel(className);
        detection.setConfidence(BigDecimal.valueOf(confidence));
        detection.setStatus(confidence >= CONFIDENCE_THRESHOLD ? 1 : 0);
        long now = System.currentTimeMillis();
        detection.setCreatedTime(now);
        detection.setUpdatedTime(now);
        pestDetectionMapper.insert(detection);

        log.info("用户{} 检测结果: class={}, confidence={}, valid={}", 
                userId, className, confidence, confidence >= CONFIDENCE_THRESHOLD);

        // 5. 置信度 < 0.7 暂正常返回，后续修改
        if (confidence < CONFIDENCE_THRESHOLD) {

        }

        // 6. 返回结果
        PestDetectionVO vo = new PestDetectionVO();
        vo.setId(detection.getId());
        vo.setUserId(userId);
        vo.setImageName(imageName);
        vo.setTopLabel(className);
        vo.setConfidence(BigDecimal.valueOf(confidence));
        vo.setCreatedTime(detection.getCreatedTime());
        return vo;
    }

    @Override
    public List<PestDetectionVO> getRecords(Long userId) {
        LambdaQueryWrapper<PestDetection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PestDetection::getUserId, userId)
               .eq(PestDetection::getStatus, 1)
               .orderByDesc(PestDetection::getCreatedTime);
        
        List<PestDetection> records = pestDetectionMapper.selectList(wrapper);
        
        List<PestDetectionVO> voList = new ArrayList<>();
        for (PestDetection record : records) {
            PestDetectionVO vo = new PestDetectionVO();
            vo.setId(record.getId());
            vo.setUserId(record.getUserId());
            vo.setImageName(record.getOriginalFileName());
            vo.setTopLabel(record.getTopLabel());
            vo.setConfidence(record.getConfidence());
            vo.setCreatedTime(record.getCreatedTime());
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 保存图片到本地目录
     */
    private String saveImageToLocal(Long userId, String imageBase64) {
        try {
            // 创建目录
            Path dirPath = Paths.get(PEST_IMAGE_DIR);
            Files.createDirectories(dirPath);

            // 生成文件名
            String fileName = userId + "_" + System.currentTimeMillis() + ".jpg";
            Path filePath = dirPath.resolve(fileName);

            // 解码并保存
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
            Files.write(filePath, imageBytes);

            log.info("图片已保存: {}", filePath);
            return filePath.toString();
        } catch (IOException e) {
            log.error("保存图片失败: {}", e.getMessage(), e);
            throw new BusinessException(CommonErrorCode.LLM_PARAM_INVALID, "保存图片失败");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callFlaskPredict(String imageBase64) {
        try {
            // Flask 接收带前缀的 base64: data:image/jpeg;base64,/9j/4AAQ...
            String fullBase64 = "data:image/jpeg;base64," + imageBase64;
            Map<String, Object> requestBody = Map.of("image_base64", fullBase64);
            
            Map<String, Object> response = modelWebClient.post()
                    .uri("/predict")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            return response;
        } catch (Exception e) {
            log.error("调用Flask服务失败: {}", e.getMessage(), e);
            return null;
        }
    }

    private String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}

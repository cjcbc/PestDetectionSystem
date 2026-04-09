package com.gzy.pestdetectionsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gzy.pestdetectionsystem.dto.PestDetectionDTO;
import com.gzy.pestdetectionsystem.entity.PestDetection;
import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.exception.CommonErrorCode;
import com.gzy.pestdetectionsystem.mapper.PestDetectionMapper;
import com.gzy.pestdetectionsystem.service.PestDetectionService;
import com.gzy.pestdetectionsystem.utils.RedisUtil;
import com.gzy.pestdetectionsystem.utils.SnowflakeIdGenerator;
import com.gzy.pestdetectionsystem.vo.PestDetectionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PestDetectionServiceImpl implements PestDetectionService {

    private static final double CONFIDENCE_THRESHOLD = 0.7;
    private static final int STATUS_NOT_DETECTED = -1;
    private static final int STATUS_LOW_CONFIDENCE = 0;
    private static final int STATUS_HIGH_CONFIDENCE = 1;
    private static final String NOT_DETECTED_LABEL = "未检测到病虫害";
    private static final String PEST_IMAGE_DIR = "D:\\SHU files\\Graduation project\\PestDetectionSystem\\pest-images";
    private static final String ANNOTATED_SUFFIX = "_annotated";
    
    private static final long DETECTION_RECORD_CACHE_TTL_SECONDS = 30 * 60;
    private static final String DETECTION_RECORD_CACHE_KEY_PREFIX = "detection:record:";

    private final PestDetectionMapper pestDetectionMapper;

    @Qualifier("modelWebClient")
    private final WebClient modelWebClient;

    private final SnowflakeIdGenerator snowflakeIdGenerator;
    
    private final RedisUtil redisUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public PestDetectionVO detect(Long userId, PestDetectionDTO dto) {
        if (dto.getImageBase64() == null || dto.getImageBase64().isBlank()) {
            throw new BusinessException(CommonErrorCode.LLM_PARAM_INVALID, "图片不能为空");
        }

        String originalImagePath = saveImageToLocal(userId, dto.getImageBase64());
        String originalImageName = extractFileName(originalImagePath);
        Map<String, Object> flaskResult = callFlaskPredict(dto.getImageBase64(), originalImageName);

        if (flaskResult == null || !Boolean.TRUE.equals(flaskResult.get("success"))) {
            throw new BusinessException(CommonErrorCode.LLM_PARAM_INVALID, "Flask 检测服务调用失败");
        }

        String annotatedPath = (String) flaskResult.get("annotated_path");
        if (annotatedPath == null || annotatedPath.isBlank()) {
            throw new BusinessException(CommonErrorCode.LLM_PARAM_INVALID, "Flask 未返回标注图片路径");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> predictions = (List<Map<String, Object>>) flaskResult.get("predictions");

        int status;
        String className;
        double confidence;
        if (predictions == null || predictions.isEmpty()) {
            status = STATUS_NOT_DETECTED;
            className = NOT_DETECTED_LABEL;
            confidence = 0D;
        } else {
            Map<String, Object> prediction = predictions.get(0);
            confidence = ((Number) prediction.get("confidence")).doubleValue();
            className = (String) prediction.get("class_name");
            status = confidence >= CONFIDENCE_THRESHOLD ? STATUS_HIGH_CONFIDENCE : STATUS_LOW_CONFIDENCE;
        }

        PestDetection detection = new PestDetection();
        detection.setId(snowflakeIdGenerator.nextId());
        detection.setUserId(userId);
        detection.setImageUrl(annotatedPath);
        detection.setOriginalFileName(originalImageName);
        detection.setResultJson(toJsonString(flaskResult));
        detection.setTopLabel(className);
        detection.setConfidence(BigDecimal.valueOf(confidence));
        detection.setStatus(status);
        long now = System.currentTimeMillis();
        detection.setCreatedTime(now);
        detection.setUpdatedTime(now);
        pestDetectionMapper.insert(detection);
        
        // 缓存检测结果
        PestDetectionVO vo = toVO(detection);
        cacheDetectionRecord(userId, vo);

        log.info("用户 {} 检测完成: class={}, confidence={}, status={}, annotatedPath={}",
                userId, className, confidence, status, annotatedPath);

        return vo;
    }

    @Override
    public List<PestDetectionVO> getRecords(Long userId) {
        // 先尝试从缓存获取
        String cacheKey = buildDetectionRecordsCacheKey(userId);
        Object cachedObject = redisUtil.get(cacheKey, Object.class);
        if (cachedObject instanceof List) {
            log.info("detection records cache hit, userId={}", userId);
            @SuppressWarnings("unchecked")
            List<PestDetectionVO> cachedRecords = (List<PestDetectionVO>) cachedObject;
            return cachedRecords;
        }
        
        log.info("detection records cache missed, userId={}", userId);
        LambdaQueryWrapper<PestDetection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PestDetection::getUserId, userId)
                .orderByDesc(PestDetection::getCreatedTime);

        List<PestDetection> records = pestDetectionMapper.selectList(wrapper);
        List<PestDetectionVO> voList = new ArrayList<>();

        for (PestDetection record : records) {
            voList.add(toVO(record));
        }
        
        // 缓存记录列表
        if (!voList.isEmpty()) {
            cacheDetectionRecords(userId, voList);
        }

        return voList;
    }

    private PestDetectionVO toVO(PestDetection detection) {
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

    private String saveImageToLocal(Long userId, String imageBase64) {
        try {
            Path dirPath = Paths.get(PEST_IMAGE_DIR);
            Files.createDirectories(dirPath);

            String fileName = userId + "_" + System.currentTimeMillis() + ".jpg";
            Path filePath = dirPath.resolve(fileName);

            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
            Files.write(filePath, imageBytes);

            log.info("原图已保存: {}", filePath);
            return filePath.toString();
        } catch (IOException e) {
            log.error("保存原图失败: {}", e.getMessage(), e);
            throw new BusinessException(CommonErrorCode.LLM_PARAM_INVALID, "保存原图失败");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callFlaskPredict(String imageBase64, String imageName) {
        try {
            String fullBase64 = "data:image/jpeg;base64," + imageBase64;
            Map<String, Object> requestBody = Map.of(
                    "image_base64", fullBase64,
                    "image_name", imageName
            );

            return modelWebClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            log.error("调用 Flask 服务失败: {}", e.getMessage(), e);
            return null;
        }
    }

    private String extractFileName(String path) {
        if (path == null || path.isBlank()) {
            return path;
        }
        return Paths.get(path).getFileName().toString();
    }

    private String getOriginalImagePathFromAnnotated(String annotatedPath) {
        if (annotatedPath == null || annotatedPath.isBlank()) {
            return annotatedPath;
        }

        Path path = Paths.get(annotatedPath);
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String nameWithoutExt = dotIndex >= 0 ? fileName.substring(0, dotIndex) : fileName;
        String extension = dotIndex >= 0 ? fileName.substring(dotIndex) : "";

        if (nameWithoutExt.endsWith(ANNOTATED_SUFFIX)) {
            nameWithoutExt = nameWithoutExt.substring(0, nameWithoutExt.length() - ANNOTATED_SUFFIX.length());
        }

        Path parent = path.getParent();
        Path originalPath = parent == null
                ? Paths.get(nameWithoutExt + extension)
                : parent.resolve(nameWithoutExt + extension);
        return originalPath.toString();
    }

    private String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
    
    private String buildDetectionRecordsCacheKey(Long userId) {
        return DETECTION_RECORD_CACHE_KEY_PREFIX + userId;
    }
    
    private void cacheDetectionRecord(Long userId, PestDetectionVO record) {
        if (record == null || userId == null) {
            return;
        }
        
        // 更新整个列表缓存时，先清除旧的缓存
        evictDetectionRecordsCache(userId);
        log.info("detection record cache updated, userId={}", userId);
    }
    
    private void cacheDetectionRecords(Long userId, List<PestDetectionVO> records) {
        if (records == null || userId == null) {
            return;
        }
        String cacheKey = buildDetectionRecordsCacheKey(userId);
        boolean cached = redisUtil.set(cacheKey, records, DETECTION_RECORD_CACHE_TTL_SECONDS);
        if (cached) {
            log.info("detection records cache updated, userId={}, ttlSeconds={}", userId, DETECTION_RECORD_CACHE_TTL_SECONDS);
        }
    }
    
    private void evictDetectionRecordsCache(Long userId) {
        if (userId == null) {
            return;
        }
        String cacheKey = buildDetectionRecordsCacheKey(userId);
        redisUtil.del(cacheKey);
        log.info("detection records cache evicted, userId={}", userId);
    }
}

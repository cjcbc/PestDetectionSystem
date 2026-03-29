package com.gzy.pestdetectionsystem.service.impl;

import com.gzy.pestdetectionsystem.service.ModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ModelServiceImpl implements ModelService {
    private final WebClient modelWebClient;

    public ModelServiceImpl(@Qualifier("modelWebClient") WebClient modelWebClient) {
        this.modelWebClient = modelWebClient;
    }

    @Override
    public Map<String, Object> calculate(double a, double b, String operation) {

        Map<String, Object> result = new HashMap<>();

        try {
            log.info("调用Flask计算接口: {} {} {}", a, operation, b);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("a", a);
            requestBody.put("b", b);
            requestBody.put("operation", operation);

            Map responseBody = modelWebClient.post()
                    .uri("/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();   // 同步阻塞获取结果

            if (responseBody != null) {
                result.put("success", true);
                result.put("data", responseBody);
                log.debug("计算完成");
            } else {
                result.put("success", false);
                result.put("error", "响应为空");
                log.warn("计算失败：响应为空");
            }

        } catch (Exception e) {

            result.put("success", false);
            result.put("message", "调用Flask服务失败: " + e.getMessage());

            log.error("调用Flask服务异常: {}", e.getMessage(), e);
        }

        return result;
    }
}

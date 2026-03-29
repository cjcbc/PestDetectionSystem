package com.gzy.pestdetectionsystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@Data
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {
    private String baseUrl;
    private String apiKey;
    private String model;
    private String chatPath;
    private Double temperature;
    private Integer maxTokens;
}

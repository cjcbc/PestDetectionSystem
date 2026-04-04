package com.gzy.pestdetectionsystem.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "user")
public class UserProperties {
    private String basePath;
}

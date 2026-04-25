package com.gzy.pestdetectionsystem.config.llm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class LlmWebClientConfig {
    @Bean("llmWebClient")
    public WebClient llmWebClient(LlmProperties llmProperties){
        return WebClient
                .builder()
                .baseUrl(llmProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}

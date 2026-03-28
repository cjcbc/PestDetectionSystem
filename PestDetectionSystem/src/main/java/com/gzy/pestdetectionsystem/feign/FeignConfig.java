package com.gzy.pestdetectionsystem.feign;

import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "yolo-service",url="localhost:5000")
public interface FeignConfig {

    @GetMapping( "/test")
    String test();
}

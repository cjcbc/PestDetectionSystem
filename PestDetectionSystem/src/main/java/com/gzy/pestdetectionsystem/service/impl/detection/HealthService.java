package com.gzy.pestdetectionsystem.service.impl.detection;

import com.gzy.pestdetectionsystem.feign.FeignConfig;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class HealthService{
    private final FeignConfig feignConfig;

    public String health(){
        return feignConfig.test();
    }
}

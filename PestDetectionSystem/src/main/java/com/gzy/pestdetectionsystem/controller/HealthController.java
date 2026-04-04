package com.gzy.pestdetectionsystem.controller;

import com.gzy.pestdetectionsystem.service.impl.HealthService;
import com.gzy.pestdetectionsystem.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {
    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }
    @GetMapping
    public Result<?> health() {
        String message = healthService.health();
        return Result.ok(message);
    }
}

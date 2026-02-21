package com.gzy.pestdetectionsystem.controller;

import com.gzy.pestdetectionsystem.service.ModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/model")
@RequiredArgsConstructor
public class ModelController {
    private final ModelService modelService;

    @PostMapping("/calculate")
    public Map<String, Object> calculate(@RequestParam double a,
                                         @RequestParam double b,
                                         @RequestParam String operation) {
        return modelService.calculate(a, b, operation);
    }
}

package com.gzy.pestdetectionsystem.service;

import java.util.Map;

public interface ModelService {
    /**
     * 调用计算接口
     * @param a 第一个数字
     * @param b 第二个数字
     * @param operation 操作类型 (add, subtract, multiply, divide)
     * @return 计算结果
     */
    Map<String, Object> calculate(double a, double b, String operation);

}

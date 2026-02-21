package com.gzy.pestdetectionsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gzy.pestdetectionsystem.mapper")
public class PestDetectionSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PestDetectionSystemApplication.class, args);
    }

}
